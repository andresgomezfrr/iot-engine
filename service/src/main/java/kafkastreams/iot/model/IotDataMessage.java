package kafkastreams.iot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

// {"id":"A", "timestamp":123456789, "metrics":{"hum":49, "temp":34}}
@JsonIgnoreProperties
public class IotDataMessage {
    @JsonProperty("timestamp")
    long timestamp;

    @JsonProperty("id")
    String id;

    @JsonProperty("location")
    String location;

    @JsonProperty("owner")
    String owner;

    @JsonProperty("metrics")
    Map<String, Integer> metrics;

    @JsonProperty("outside_temperature")
    Integer outside_temperature;
    @JsonProperty("outside_humidity")
    Integer outside_humidity;

    @JsonCreator
    public IotDataMessage(
            @JsonProperty("timestamp") long timestamp,
            @JsonProperty("id") String id,
            @JsonProperty("location") String location,
            @JsonProperty("owner") String owner,
            @JsonProperty("metrics") Map<String, Integer> metrics,
            @JsonProperty("outside_temperature") Integer outside_temperature,
            @JsonProperty("outside_humidity") Integer outside_humidity

            ) {
        this.timestamp = timestamp;
        this.id = id;
        this.location = location;
        this.owner = owner;
        this.metrics = metrics;
        this.outside_temperature = outside_temperature;
        this.outside_humidity = outside_humidity;
    }

    public IotDataMessage(IotDataMessage dataMessage, IotOutsideMetrics outsideMetrics){
        this.id = dataMessage.id;
        this.timestamp = dataMessage.timestamp;
        this.metrics = dataMessage.metrics;
        this.location = dataMessage.location;
        this.owner = dataMessage.owner;

        if(outsideMetrics != null) {
            this.outside_temperature = outsideMetrics.temperature;
            this.outside_humidity = outsideMetrics.humidity;
        }
    }

    public IotDataMessage(IotDataMessage dataMessage, IotMetadataSensor metadataSensor){
        this.id = dataMessage.id;
        this.timestamp = dataMessage.timestamp;
        this.metrics = dataMessage.metrics;
        if(metadataSensor != null) {
            this.location = metadataSensor.location;
            this.owner = metadataSensor.owner;
        }
    }

    @JsonProperty
    public Integer getOutside_temperature() {
        return outside_temperature;
    }

    @JsonProperty
    public void setOutside_temperature(Integer outside_temperature) {
        this.outside_temperature = outside_temperature;
    }

    @JsonProperty
    public Integer getOutside_humidity() {
        return outside_humidity;
    }

    @JsonProperty
    public void setOutside_humidity(Integer outside_humidity) {
        this.outside_humidity = outside_humidity;
    }

    @JsonProperty
    public <T> T getMetrics(String metricName) {
        return (T) metrics.get(metricName);
    }

    @JsonProperty
    public String getLocation() {
        return location;
    }

    @JsonProperty
    public void setLocation(String location) {
        this.location = location;
    }

    @JsonProperty
    public String getOwner() {
        return owner;
    }

    @JsonProperty
    public void setOwner(String owner) {
        this.owner = owner;
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
