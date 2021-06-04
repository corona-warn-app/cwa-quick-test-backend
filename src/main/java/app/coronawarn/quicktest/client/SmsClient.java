package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.SmsMessage;
import app.coronawarn.quicktest.model.SmsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "smsClient",
    url = "${otc-sms-server.url}"
)
public interface SmsClient {

    @PostMapping(value = "/notifications/sms",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    SmsResponse publishSms(@RequestBody SmsMessage smsMessage);
}
