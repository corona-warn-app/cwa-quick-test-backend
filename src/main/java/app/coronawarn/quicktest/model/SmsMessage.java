package app.coronawarn.quicktest.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmsMessage {

    private static final String COUNTRY_CODE_GERMANY = "+49";

    @NotNull
    String endpoint;

    @NotNull
    @Size(min = 1)
    String message;

    public static class SmsMessageBuilder {
        /**
         * Create an SMS endpoint according to E.164 standard.
         * @param endpoint SMS Endpoint, phonenumber in E.164 format (+cc) or local format (017..)
         * @return phonenumber in E.164 format
         */
        public SmsMessageBuilder endpoint(String endpoint) {
            this.endpoint = endpoint.startsWith("0")
                ? endpoint.replaceFirst("0", COUNTRY_CODE_GERMANY) : endpoint;
            return this;
        }
    }
}
