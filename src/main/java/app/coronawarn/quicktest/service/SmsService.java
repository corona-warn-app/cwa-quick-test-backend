package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.SmsClient;
import app.coronawarn.quicktest.config.SmsConfig;
import app.coronawarn.quicktest.model.SmsMessage;
import app.coronawarn.quicktest.model.SmsResponse;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsClient smsClient;

    private final SmsConfig smsConfig;


    /**
     * Sends a SMS containing the PDF password to the tested person.
     * @param receiver Phonenumber in E.164 format
     */
    public void sendPasswordSms(String receiver, String password) {
        if (smsConfig.isEnabled()) {
            String messageText = String.format(smsConfig.getMessageTemplate(), password);
            SmsMessage message = new SmsMessage(receiver, messageText);
            log.debug("Sending sms: {}", message);
            SmsResponse response = smsClient.publishSms(message);
            log.debug("SMS Client response: {}", response);
        }
        //TODO Error handling when OTC Api is defined
    }

    /**
     * Creates a random digit password.
     * @return Password depending on configured passwordLength.
     */
    public String createPassword() {
        int passwordLength = smsConfig.getPasswordLength();
        int number = new Random().nextInt((int) Math.pow(10, passwordLength) - 1);
        return String.format("%0" + passwordLength + "d", number);
    }
}
