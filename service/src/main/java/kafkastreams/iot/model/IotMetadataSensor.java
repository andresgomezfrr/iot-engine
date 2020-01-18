package kafkastreams.iot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

//A,{"id":"A", "rules":[{"ruleName":"max_temperature", "metricName":"temp", "metricValue":22, "condition":">"}, {"ruleName":"min_humidity", "metricName":"hum", "metricValue":50, "condition":"<"}]}
@JsonIgnoreProperties
public class IotMetadataSensor {
    @JsonProperty("sensor")
    String sensor;

    @JsonProperty("location")
    String location;

    @JsonProperty("owner")
    String owner;


    @JsonCreator
    public IotMetadataSensor(@JsonProperty("sensor") String sensor,
                             @JsonProperty("location") String location,
                             @JsonProperty("owner") String owner
    ) {
this.sensor =sensor;
this.location = location;
this.owner = owner;
    }

    @JsonProperty
    public String getSensor() {
        return sensor;
    }

    @JsonProperty
    public void setSensor(String sensor) {
        this.sensor = sensor;
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

}
