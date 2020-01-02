package kafkastreams.iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class Config {
    Properties streamProperties = new Properties();
    Map<String, Object> config;


    public Config(String file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(new File(file), Map.class);

        config.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("stream."))
                .forEach(entry ->
                        streamProperties.put(
                                entry.getKey().replace("stream.", ""),
                                entry.getValue().toString()
                        )
                );

        streamProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
    }

    public Properties getStreamProperties() {
        return streamProperties;
    }

    public void addStreamProperty(String propertyName, String propertyValue) {
        streamProperties.put(propertyName, propertyValue);
    }

    public String getDataTopic() {
        return (String) config.getOrDefault("topic.data", "data");
    }

    public String getRuleTopic() {
        return (String) config.getOrDefault("topic.rule", "rule");
    }

    public String getAggTopic() {
        return (String) config.getOrDefault("topic.agg", "agg");
    }

    public String getAlertTopic() {
        return (String) config.getOrDefault("topic.alert", "alert");
    }

    public String getControlTopic() {
        return (String) config.getOrDefault("topic.control", "control");
    }

    public Integer getRetentionTimeDays() {
        return (Integer) config.getOrDefault("retention.time.days", 7);
    }

    public <T> T getConfig(String configName) {
        return (T) config.get(configName);
    }
}
