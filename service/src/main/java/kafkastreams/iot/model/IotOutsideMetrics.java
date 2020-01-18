package kafkastreams.iot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

//A,{"id":"A", "rules":[{"ruleName":"max_temperature", "metricName":"temp", "metricValue":22, "condition":">"}, {"ruleName":"min_humidity", "metricName":"hum", "metricValue":50, "condition":"<"}]}
@JsonIgnoreProperties
public class IotOutsideMetrics {

    @JsonProperty("timestamp")
    Long timestamp;

    @JsonProperty("location")
    String location;

    @JsonProperty("temperature")
    Integer temperature;

    @JsonProperty("humidity")
    Integer humidity;

    @JsonProperty("pressure")
    Integer pressure;


    @JsonCreator
    public IotOutsideMetrics(@JsonProperty("temperature") Integer temperature,
                             @JsonProperty("humidity") Integer humidity,
                             @JsonProperty("pressure") Integer pressure,
                             @JsonProperty("location") String location
    ) {
        this.location = location;
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
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
    public Integer getTemperature() {
        return temperature;
    }

    @JsonProperty
    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    @JsonProperty
    public Integer getHumidity() {
        return humidity;
    }

    @JsonProperty
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Integer getPressure() {
        return pressure;
    }

    @JsonProperty
    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }
}
