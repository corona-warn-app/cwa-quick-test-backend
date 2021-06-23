package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.EmailConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Attachment;
import app.coronawarn.quicktest.model.EmailMessage;
import java.util.Collections;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;

    @Value("${spring.mail.from}")
    private String from;

    private void sendMail(EmailMessage email, Attachment file) throws EmailServiceException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(email.getReceivers().toArray(new String[0]));
            helper.setSubject(email.getSubject());
            helper.setText(email.getText(), true);
            if (file != null) {
                helper.addAttachment(file.getName(), new ByteArrayResource(file.getData()));
            }
            try {
                mailSender.send(message);
            } catch (MailException e) {
                log.error("Error while sending email: MailException");
                throw new EmailServiceException(EmailServiceException.Reason.SEND_MESSAGE_FAILED);
            }
        } catch (MessagingException e) {
            log.error("Error while preparing email: MessagingException");
            throw new EmailServiceException(EmailServiceException.Reason.CREATE_MESSAGE_FAILED);
        }
    }

    /**
     * Sends an email with result as pdf attachment to tested person.
     *
     * @param quickTest Quicktest of tested person
     * @param pdf   Created pdf with test result
     */
    public void sendMailToTestedPerson(QuickTestArchive quickTest, byte[] pdf) throws EmailServiceException {
        String email = quickTest.getEmail();
        if (StringUtils.isBlank(email)) {
            return;
        }
        EmailConfig.TestedPerson testedPersonConfig = emailConfig.getTestedPerson();
        EmailMessage message = new EmailMessage();
        message.setSubject(testedPersonConfig.getSubject());
        String text = testedPersonConfig.getTitleText() + quickTest.getFirstName() + " " + quickTest.getLastName() + ","
                + testedPersonConfig.getText();
        message.setText(text);
        message.setReceivers(Collections.singletonList(email));
        Attachment file = new Attachment();
        file.setData(pdf);
        file.setName(testedPersonConfig.getAttachmentFilename());
        sendMail(message, file);
    }

    /**
     * Sends an email.
     * @param email Email address of local health department
     * @param pdf Created pdf with test result and personal data of tested person
     */
    public void sendMailToHealthDepartment(String email, byte[] pdf) throws EmailServiceException {
        if (StringUtils.isBlank(email)) {
            throw new EmailServiceException(EmailServiceException.Reason.INVALID_EMAIL_ADDRESS);
        }
        EmailConfig.HealthDepartment healthDepartmentConfig = emailConfig.getHealthDepartment();
        EmailMessage message = new EmailMessage();
        message.setSubject(healthDepartmentConfig.getSubject());
        message.setText(healthDepartmentConfig.getText());
        message.setReceivers(Collections.singletonList(email));
        Attachment file = new Attachment();
        file.setData(pdf);
        file.setName(healthDepartmentConfig.getAttachmentFilename());
        sendMail(message, file);
    }

    @Getter
    public static class EmailServiceException extends Exception {
        private final Reason reason;

        public EmailServiceException(Reason reason) {
            super();
            this.reason = reason;
        }

        public enum Reason {
            INVALID_EMAIL_ADDRESS,
            SEND_MESSAGE_FAILED,
            CREATE_MESSAGE_FAILED
        }
    }

}
