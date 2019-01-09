package kafkastreams.iot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IotAlertMessage {
    @JsonProperty
    String id;

    @JsonProperty
    String metricName;

    @JsonProperty
    String ruleName;

    @JsonProperty
    String payload;

    @JsonCreator
    public IotAlertMessage(
            @JsonProperty("id") String id,
            @JsonProperty("metricName") String metricName,
            @JsonProperty("ruleName") String ruleName,
            @JsonProperty("payload") String payload
    ) {
        this.id = id;
        this.metricName = metricName;
        this.ruleName = ruleName;
        this.payload = payload;
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
    public String getMetricName() {
        return metricName;
    }

    @JsonProperty
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    @JsonProperty
    public String getRuleName() {
        return ruleName;
    }

    @JsonProperty
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    @JsonProperty
    public String getPayload() {
        return payload;
    }

    @JsonProperty
    public void setPayload(String payload) {
        this.payload = payload;
    }
}
