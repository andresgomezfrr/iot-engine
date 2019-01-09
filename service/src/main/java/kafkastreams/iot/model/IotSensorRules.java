package kafkastreams.iot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

//A,{"id":"A", "rules":[{"ruleName":"max_temperature", "metricName":"temp", "metricValue":22, "condition":">"}, {"ruleName":"min_humidity", "metricName":"hum", "metricValue":50, "condition":"<"}]}
public class IotSensorRules {
    @JsonProperty("id")
    String id;

    @JsonProperty("rules")
    List<IotSensorRule> rules;


    @JsonCreator
    public IotSensorRules(@JsonProperty("id") String id, @JsonProperty("rules") List<IotSensorRule> rules) {
        this.id = id;
        this.rules = rules;
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
    public List<IotSensorRule> getRules() {
        return rules;
    }

    @JsonProperty
    public void setRules(List<IotSensorRule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append("[");

        for (IotSensorRule rule : rules) {
            builder.append(rule.toString());
        }

        builder.append("]");

        return builder.toString();
    }
}
