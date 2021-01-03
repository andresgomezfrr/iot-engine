package kafkastreams.iot;

import kafkastreams.iot.restserver.RestService;
import kafkastreams.iot.streams.StreamBuilder;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.HostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class IotEngine {
    private static final Logger log = LoggerFactory.getLogger(IotEngine.class);

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            Config config = new Config(args[0]);

            final KafkaStreams streams = new KafkaStreams(
                    StreamBuilder.getStreamToplogy(
                            config.getDataTopic(), config.getRuleTopic(),
                            config.getAlertTopic(), config.getAggTopic(),
                            config.getControlTopic(), config.getSensorMetadataTopic(), config.getRetentionTimeDays()
                    ),
                    config.getStreamProperties()
            );

            streams.start();

            String[] appServerEndpoint = ((String) config.getConfig("stream.application.server")).split(":");
            String[] restServerEndpoint = ((String) config.getConfig("rest.server.endpoint")).split(":");
            RestService restService = new RestService(
                    config,
                    streams,
                    new HostInfo(appServerEndpoint[0], Integer.parseInt(appServerEndpoint[1])),
                    new HostInfo(restServerEndpoint[0], Integer.parseInt(restServerEndpoint[1]))
            );
            restService.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Closing IoT engine.");

                try {
                    streams.close(Duration.ofMinutes(1));
                    restService.stop();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                log.info("Stopped IoT engine.");
            }));

            log.info("Started IoT engine.");
            log.info("Listen on http://{}/", (String) config.getConfig("rest.server.endpoint"));
        } else {
            log.info("Usage: java -jar iot-engine.jar <config.json>");
            log.info("Example: java -jar iot-engine.jar ./config.json");
        }
    }
}
