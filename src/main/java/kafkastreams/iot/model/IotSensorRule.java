package kafkastreams.iot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.Function;

public class IotSensorRule {
    @JsonProperty("ruleName")
    String ruleName;

    @JsonProperty("metricName")
    String metricName;

    @JsonProperty("metricValue")
    Integer metricValue;

    Function<Integer, Boolean> conditionEval;

    @JsonProperty("condition")
    String condition;

    @JsonCreator
    public IotSensorRule(
            @JsonProperty("ruleName") String ruleName,
            @JsonProperty("metricName") String metricName,
            @JsonProperty("metricValue") Integer metricValue,
            @JsonProperty("condition") String condition
    ) {
        this.ruleName = ruleName;
        this.metricName = metricName;
        this.metricValue = metricValue;

        this.condition = condition;

        if (condition.equals("==")) {
            conditionEval = (value) -> value == metricValue;
        } else if (condition.equals(">")) {
            conditionEval = (value) -> value > metricValue;
        } else if (condition.equals("<")) {
            conditionEval = (value) -> value < metricValue;
        } else if (condition.equals(">=")) {
            conditionEval = (value) -> value >= metricValue;
        } else if (condition.equals("<=")) {
            conditionEval = (value) -> value <= metricValue;
        } else if (condition.equals("!=")) {
            conditionEval = (value) -> value != metricValue;
        } else {
            conditionEval = (value) -> false;
        }
    }

    public Boolean eval(Integer value) {
        return conditionEval.apply(value);
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
    public Integer getMetricValue() {
        return metricValue;
    }

    @JsonProperty
    public void setMetricValue(Integer metricValue) {
        this.metricValue = metricValue;
    }

    @JsonProperty
    public String getCondition() {
        return condition;
    }

    @JsonProperty
    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "{" + "ruleName" + ":" + ruleName + ", " +
                "metricName" + ":" + metricName + ", " +
                "metricValue" + ":" + metricValue + ", " +
                "condition" + ":" + condition +
                "}";
    }
}
