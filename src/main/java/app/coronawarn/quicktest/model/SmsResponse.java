package app.coronawarn.quicktest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SmsResponse {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("message_id")
    private String messageId;
}
