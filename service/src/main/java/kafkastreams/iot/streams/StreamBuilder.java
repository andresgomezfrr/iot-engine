package kafkastreams.iot.streams;

import kafkastreams.iot.model.*;
import kafkastreams.iot.model.serders.IotSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StreamBuilder {
    private static final Logger log = LoggerFactory.getLogger(StreamBuilder.class);
    public static final String RULES_STORE_NAME = "rules";
    public static final String OUTSIDE_STORE_NAME = "outside";
    public static final String METADATA_SENSOR_STORE_NAME = "metadata-sensor";
    public static final String AGG_STORE_NAME = "agg-metrics";

    public static Topology getStreamToplogy(String inTopic, String rulesTopic,
                                            String alertTopic, String aggTopic,
                                            String controlTopic, Integer retentionTimeDays) {
        StreamsBuilder builder = new StreamsBuilder();
        IotSerde iotDataMessageSerde = new IotSerde<>(IotDataMessage.class);
        IotSerde iotSensorRulesSerde = new IotSerde<>(IotSensorRules.class);
        IotSerde iotMetadataSensor = new IotSerde<>(IotMetadataSensor.class);
        IotSerde iotOutsideMetricsSerde = new IotSerde<>(IotOutsideMetrics.class);

        KStream<String, IotDataMessage> inStream = builder.stream(
                inTopic,
                Consumed.with(
                        Serdes.String(),
                        iotDataMessageSerde,
                        (record, lastTime) ->
                                record.value() != null ? ((IotDataMessage) record.value()).getTimestamp() * 1000L : -1L,
                        Topology.AutoOffsetReset.LATEST
                )
        );

        KTable<String, IotSensorRules> rulesTable = builder.table(rulesTopic,
                Consumed.with(Serdes.String(), iotSensorRulesSerde),
                Materialized.<String, IotSensorRules, KeyValueStore<Bytes, byte[]>>as(RULES_STORE_NAME)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(iotSensorRulesSerde)
        );

        KStream<String, IotDataMessage> partitionedInStream = inStream
                .selectKey((k, v) -> v.getId());

        // Build alert stream

        KStream<String, IotAlertMessage> controlAlertStream = partitionedInStream
                .leftJoin(rulesTable, (message, rules) -> {
                    List<IotAlertMessage> alerts = new ArrayList<>();
                    log.debug("Processing message {} with rules {} :", message, rules);

                    if (message != null && rules != null) {
                        Map<String, Integer> metrics = message.getMetrics();

                        if (rules != null && metrics != null) {
                            for (IotSensorRule rule : rules.getRules()) {
                                if (metrics.containsKey(rule.getMetricName())) {
                                    Integer metricValue = metrics.get(rule.getMetricName());
                                    log.debug(" * Processing message {} with rule {}", message, rule);
                                    if (rule.eval(metricValue)) {
                                        alerts.add(
                                                new IotAlertMessage(
                                                        rules.getId(), rule.getMetricName(), rule.getRuleName(),
                                                        String.format(
                                                                "%s %s %s",
                                                                metricValue,
                                                                rule.getCondition(),
                                                                rule.getMetricValue()
                                                        ),
                                                        rule.getControlAction()
                                                )
                                        );
                                    }
                                }
                            }
                        }
                    }

                    return alerts;
                })
                .flatMap((key, value) -> value.stream().map(v -> new KeyValue<>(key, v)).collect(Collectors.toList()));

        controlAlertStream
                .to(alertTopic, Produced.with(Serdes.String(), new IotSerde<>(IotAlertMessage.class)));

        controlAlertStream
                .filter((key, value) -> value.getControlAction() != null)
                .mapValues(value -> new IotControlMessage(value.getId(), value.getControlAction()))
                .to(controlTopic, Produced.with(Serdes.String(), new IotSerde<>(IotControlMessage.class)));

        // Build metric storage stream
        KGroupedStream<String, IotDataMessage> groupedByData = partitionedInStream
                .groupBy((key, word) -> key, Grouped.with(Serdes.String(), iotDataMessageSerde));

        KStream<String, IotDataMessage> groupdMetricsStreams = groupedByData
                .windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
                .aggregate(
                        IotDataAggregator::new,
                        (key, value, aggregate) -> aggregate.agg(key, value),
                        Materialized.<String, IotDataAggregator, WindowStore<Bytes, byte[]>>as(AGG_STORE_NAME)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new IotSerde<>(IotDataAggregator.class))
                                .withRetention(Duration.ofDays(retentionTimeDays))
                )
                .toStream()
                .map(
                        (key, value) ->
                                new KeyValue<>(key.key(),
                                        new IotDataMessage(
                                                key.window().endTime().getEpochSecond(),
                                                key.key(),
                                                null,
                                                null,
                                                value.generateAggMetrics(),
                                                null,
                                                null
                                        )
                                )
                );
                // TODO: Change output format to split metrics inside multiple messages. One message per metric.

        KTable<String, IotMetadataSensor> metadataSensorTable = builder.table("sensor-metadata",
                Consumed.with(Serdes.String(), iotMetadataSensor),
                Materialized.<String, IotSensorRules, KeyValueStore<Bytes, byte[]>>as(METADATA_SENSOR_STORE_NAME)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(iotMetadataSensor)
        );

        KStream<String, IotDataMessage> streamWithMetadata = groupdMetricsStreams.leftJoin(metadataSensorTable,
                (iotDataMessage, iotMetadataSensor1) -> new IotDataMessage(iotDataMessage, iotMetadataSensor1),
                Joined.with(Serdes.String(), new IotSerde<>(IotDataMessage.class), new IotSerde<>(IotMetadataSensor.class))
        );

        GlobalKTable<String, IotOutsideMetrics> outsideMetricsGlobalKTable =  builder.globalTable("weather", Consumed.with(Serdes.String(), iotOutsideMetricsSerde),
                Materialized.<String, IotSensorRules, KeyValueStore<Bytes, byte[]>>as(OUTSIDE_STORE_NAME)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(iotOutsideMetricsSerde));

        streamWithMetadata.leftJoin(outsideMetricsGlobalKTable,
                (key, value) -> value.getLocation(),
                (value1, value2) -> new IotDataMessage(value1, value2)
        ).to(aggTopic, Produced.with(Serdes.String(), new IotSerde<>(IotDataMessage.class)));


        return builder.build();
    }
}
