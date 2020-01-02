package kafkastreams.iot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageResponse {
    @JsonProperty
    String status;

    @JsonProperty
    String errorMessage;

    @JsonCreator
    public MessageResponse(@JsonProperty("status") String status, @JsonProperty("errorMessage") String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }


    @JsonProperty
    public String getStatus() {
        return status;
    }

    @JsonProperty
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
