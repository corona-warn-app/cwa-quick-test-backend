package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.model.Attachment;
import app.coronawarn.quicktest.model.EmailMessage;
import java.util.Collections;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
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

    @Value("${spring.mail.from}")
    private String from;

    private void sendMail(EmailMessage email, Attachment file) {
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
            }
        } catch (MessagingException e) {
            log.error("Error while preparing email: MessagingException");
        }
    }

    /**
     * Sends an email with result as pdf attachment to tested person.
     *
     * @param email Email address of tested person
     * @param pdf   Created pdf with test result
     */
    public void sendMailToTestedPerson(String email, byte[] pdf) {
        if (StringUtils.isBlank(email)) {
            return;
        }
        EmailMessage message = new EmailMessage();
        message.setSubject("Ihr Testergebnis");
        message.setText("Hallo. Anbei ihr Testergebnis.");
        message.setReceivers(Collections.singletonList(email));
        Attachment file = new Attachment();
        file.setData(pdf);
        file.setName("Schnelltest.pdf");
        sendMail(message, file);
    }

    /**
     * Sends an email.
     * @param email Email address of local health department
     * @param pdf Created pdf with test result and personal data of tested person
     */
    public void sendMailToHealthDepartment(String email, byte[] pdf) {
        if (StringUtils.isBlank(email)) {
            return;
        }
        EmailMessage message = new EmailMessage();
        message.setSubject("subject");
        message.setText("");
        message.setReceivers(Collections.singletonList(email));
        Attachment file = new Attachment();
        file.setData(pdf);
        file.setName("Schnelltest.pdf");
        sendMail(message, file);
    }

}
