package app.coronawarn.quicktest.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SmsMessage {

    //TODO: E.164 Validator
    @NotNull
    String endpoint;

    @NotNull
    @Size(min = 1)
    String message;
}
