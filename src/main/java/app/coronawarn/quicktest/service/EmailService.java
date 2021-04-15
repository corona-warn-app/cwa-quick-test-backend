package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.model.Attachment;
import app.coronawarn.quicktest.model.EmailMessage;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    /**
     * Sends an email.
     */
    public void sendMail(EmailMessage email, Attachment file) {
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
                System.out.println(e.getMessage());
            }
        } catch (MessagingException e) {
            System.out.println("MessagingException");
            System.out.println(e.getMessage());
        }
    }

}
