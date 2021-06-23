/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.SmsClient;
import app.coronawarn.quicktest.config.SmsConfig;
import app.coronawarn.quicktest.exception.SmsException;
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
    public void sendPasswordSms(String receiver, String password) throws SmsException {
        if (smsConfig.isEnabled()) {
            String messageText = String.format(smsConfig.getMessageTemplate(), password);
            SmsMessage message = SmsMessage.builder()
              .endpoint(receiver)
              .message(messageText)
              .build();
            log.debug("Sending sms: {}", message);

            SmsResponse response = smsClient.send(message);
            log.debug("Returned: {}", response);
        }
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
