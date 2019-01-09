package kafkastreams.iot;

import kafkastreams.iot.restserver.RestService;
import kafkastreams.iot.streams.StreamBuilder;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.HostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IotEngine {
    private static final Logger log = LoggerFactory.getLogger(IotEngine.class);

    public static void main(String[] args) throws Exception {
        Config config = new Config(args[0]);

        final KafkaStreams streams = new KafkaStreams(
                StreamBuilder.getStreamToplogy(
                        config.getDataTopic(), config.getRuleTopic(),
                        config.getAlertTopic(), config.getAggTopic()
                ),
                config.getStreamProperties()
        );

        streams.start();

        String[] endpoint = ((String) config.getConfig("stream.application.server")).split(":");
        RestService restService = new RestService(streams, new HostInfo(endpoint[0], Integer.parseInt(endpoint[1])));
        restService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Closing IoT engine.");

            try {
                streams.close();
                restService.stop();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            log.info("Stopped IoT engine.");
        }));

        log.info("Started IoT engine.");
        log.info("Listen on http://{}/", (String) config.getConfig("stream.application.server"));
    }
}
