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

import app.coronawarn.quicktest.config.EmailConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.TestResult;
import app.coronawarn.quicktest.utils.PdfGenerator;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final EmailConfig emailConfig;
    private final PdfGenerator pdfGenerator;
    private final HealthDepartmentService healthDepartmentService;

    /**
     * Handles eMail Notification of Patient and Healthdepartment, including encrypting PDF.
     *
     * @param quickTest  The realted quicktest
     * @param rawPdf     Unencrypted PDF
     * @param pocZipCode Zipcode used to find the related Healthdepartment
     */
    public void handleMailNotification(QuickTestArchive quickTest, byte[] rawPdf, String pocZipCode) {
        boolean emailConfirmation = quickTest.getEmailNotificationAgreement() != null
          ? quickTest.getEmailNotificationAgreement() : false;

        String pwd = smsService.createPassword();

        if (emailConfig.getTestedPerson() != null && emailConfig.getTestedPerson().isEnabled() && emailConfirmation) {
            try {
                byte[] encryptedPdf = pdfGenerator.encryptPdf(rawPdf, pwd).toByteArray();
                emailService.sendMailToTestedPerson(quickTest, encryptedPdf);
                smsService.sendPasswordSms(quickTest.getPhoneNumber(), pwd);
            } catch (IOException e) {
                log.error("Error encrypting existing pdf for tested person.");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error encrypting existing pdf.");
            } catch (EmailService.EmailServiceException emailServiceException) {
                log.error("Could not send mail to tested person.");
            }
        }
        if (emailConfig.getHealthDepartment() != null && emailConfig.getHealthDepartment().isEnabled()
            && quickTest.getTestResult() == TestResult.POSITIVE.getValue()) {
            try {
                String emailAddress = healthDepartmentService.findHealthDepartmentEmailByZipCode(pocZipCode);
                byte[] encryptedPdf = pdfGenerator.encryptPdf(rawPdf, pocZipCode).toByteArray();
                emailService.sendMailToHealthDepartment(emailAddress, encryptedPdf);
                quickTest.setHealthDepartmentInformed(true);
            } catch (EmailService.EmailServiceException e) {
                log.error("Could not send mail to hd.");
                quickTest.setHealthDepartmentInformed(false);
            } catch (IOException e) {
                log.error("Error encrypting existing pdf for health authority.");
                quickTest.setHealthDepartmentInformed(false);
            }
        }
    }


}
