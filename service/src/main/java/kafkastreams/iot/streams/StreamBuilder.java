package kafkastreams.iot.streams;

import kafkastreams.iot.model.IotAlertMessage;
import kafkastreams.iot.model.IotDataMessage;
import kafkastreams.iot.model.IotSensorRule;
import kafkastreams.iot.model.IotSensorRules;
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
    public static final String AGG_STORE_NAME = "agg-metrics";

    public static Topology getStreamToplogy(String inTopic, String rulesTopic, String alertTopic, String aggTopic) {
        StreamsBuilder builder = new StreamsBuilder();
        IotSerde iotDataMessageSerde = new IotSerde<>(IotDataMessage.class);
        IotSerde iotSensorRulesSerde = new IotSerde<>(IotSensorRules.class);

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

        partitionedInStream
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
                                        alerts.add(new IotAlertMessage(
                                                        rules.getId(), rule.getMetricName(), rule.getRuleName(),
                                                        String.format(
                                                                "%s %s %s",
                                                                metricValue,
                                                                rule.getCondition(),
                                                                rule.getMetricValue()
                                                        )
                                                )
                                        );
                                    }
                                }
                            }
                        }
                    }

                    return alerts;
                })
                .flatMap((key, value) -> value.stream().map(v -> new KeyValue<>(key, v)).collect(Collectors.toList()))
                .to(alertTopic, Produced.with(Serdes.String(), new IotSerde<>(IotAlertMessage.class)));

        // TODO: Build control stream based on alert stream

        // Build metric storage stream
        KGroupedStream<String, IotDataMessage> groupedByData = partitionedInStream
                .groupBy((key, word) -> key, Grouped.with(Serdes.String(), iotDataMessageSerde));

        groupedByData
                .windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
                .aggregate(
                        IotDataAggregator::new,
                        (key, value, aggregate) -> aggregate.agg(key, value),
                        Materialized.<String, IotDataAggregator, WindowStore<Bytes, byte[]>>as(AGG_STORE_NAME)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(new IotSerde<>(IotDataAggregator.class))
                )
                .toStream()
                .map(
                        (key, value) ->
                                new KeyValue<>(key.key(),
                                        new IotDataMessage(
                                                key.window().endTime().getEpochSecond(),
                                                key.key(),
                                                value.generateAggMetrics()
                                        )
                                )
                )
                // TODO: Change output format to split metrics inside multiple messages
                .to(aggTopic, Produced.with(Serdes.String(), new IotSerde<>(IotDataMessage.class)));

        // TODO: Add stream with static data context

        // TODO: Add stream with latest metric data

        return builder.build();
    }
}
