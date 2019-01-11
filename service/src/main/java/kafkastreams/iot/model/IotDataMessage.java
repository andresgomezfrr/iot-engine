package kafkastreams.iot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

// {"id":"A", "timestamp":123456789, "metrics":{"hum":49, "temp":34}}
public class IotDataMessage {
    @JsonProperty("timestamp")
    long timestamp;

    @JsonProperty("id")
    String id;

    @JsonProperty("metrics")
    Map<String, Integer> metrics;

    @JsonProperty("context")
    Map<String, Object> context;

    @JsonCreator
    public IotDataMessage(
            @JsonProperty("timestamp") long timestamp,
            @JsonProperty("id") String id,
            @JsonProperty("metrics") Map<String, Integer> metrics,
            @JsonProperty("context") Map<String, Object> context
    ) {
        this.timestamp = timestamp;
        this.id = id;
        this.metrics = metrics;
        this.context = context;
    }

    public <T> T getMetrics(String metricName) {
        return (T) metrics.get(metricName);
    }

    @JsonProperty
    public long getTimestamp() {
        return timestamp;
    }

    @JsonProperty
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty
    public Map<String, Integer> getMetrics() {
        return metrics;
    }

    @JsonProperty
    public void setMetrics(Map<String, Integer> metrics) {
        this.metrics = metrics;
    }

    @JsonProperty
    public Map<String, Object> getContext() {
        return context;
    }

    @JsonProperty
    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("{")
                .append("timestamp").append(":").append(timestamp).append(", ")
                .append("id").append(":").append(id).append(", ")
                .append("metrics").append(":").append(metrics)
                .append("}")
                .toString();
    }
}
