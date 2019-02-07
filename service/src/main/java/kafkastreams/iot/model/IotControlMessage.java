package kafkastreams.iot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IotControlMessage {
    @JsonProperty
    String id;

    @JsonProperty("controlAction")
    String controlAction;

    @JsonCreator
    public IotControlMessage(
            @JsonProperty("id") String id,
            @JsonProperty("controlAction") String controlAction
    ) {
        this.id = id;
        this.controlAction = controlAction;
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
    public String getControlAction() {
        return controlAction;
    }

    @JsonProperty
    public void setControlAction(String controlAction) {
        this.controlAction = controlAction;
    }
}
