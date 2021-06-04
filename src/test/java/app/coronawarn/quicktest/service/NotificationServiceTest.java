package app.coronawarn.quicktest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.EmailConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.utils.PdfGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService underTest;

    @Mock
    private EmailService emailService;
    @Mock
    private SmsService smsService;
    @Mock
    private EmailConfig emailConfig;
    @Mock
    private PdfGenerator pdfGenerator;
    @Mock
    private HealthDepartmentService healthDepartmentService;

    @Test
    void sendMailInUpdateQuickTestNegative() throws EmailService.EmailServiceException, IOException {

        EmailConfig.TestedPerson tp = new EmailConfig.TestedPerson();
        tp.setEnabled(true);
        EmailConfig.HealthDepartment hd = new EmailConfig.HealthDepartment();
        hd.setEnabled(true);


        when(emailConfig.getTestedPerson()).thenReturn(tp);
        when(emailConfig.getHealthDepartment()).thenReturn(hd);
        when(pdfGenerator.encryptPdf(any(), any())).thenReturn(new ByteArrayOutputStream());

        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setEmailNotificationAgreement(true);
        quickTestArchive.setTestResult((short) 6);
        underTest.handleMailNotification(quickTestArchive, new byte[5], "12345");
        verify(emailService, times(1)).sendMailToTestedPerson(any(), any());
        verify(emailService, never()).sendMailToHealthDepartment(any(), any());

    }

    @Test
    void sendMailInUpdateQuickTestPositive() throws EmailService.EmailServiceException, IOException {

        EmailConfig.TestedPerson tp = new EmailConfig.TestedPerson();
        tp.setEnabled(true);
        EmailConfig.HealthDepartment hd = new EmailConfig.HealthDepartment();
        hd.setEnabled(true);


        when(emailConfig.getTestedPerson()).thenReturn(tp);
        when(emailConfig.getHealthDepartment()).thenReturn(hd);
        when(pdfGenerator.encryptPdf(any(), any())).thenReturn(new ByteArrayOutputStream());

        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setEmailNotificationAgreement(true);
        quickTestArchive.setTestResult((short) 7);
        underTest.handleMailNotification(quickTestArchive, new byte[5], "12345");
        verify(emailService, times(1)).sendMailToTestedPerson(any(), any());
        verify(emailService, times(1)).sendMailToHealthDepartment(any(), any());

    }

    @Test
    void sendMailInUpdateQuickDisabledTest() throws EmailService.EmailServiceException {

        EmailConfig.TestedPerson tp = new EmailConfig.TestedPerson();
        tp.setEnabled(false);
        EmailConfig.HealthDepartment hd = new EmailConfig.HealthDepartment();
        hd.setEnabled(false);


        when(emailConfig.getTestedPerson()).thenReturn(tp);
        when(emailConfig.getHealthDepartment()).thenReturn(hd);

        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setEmailNotificationAgreement(true);
        quickTestArchive.setTestResult((short) 7);
        underTest.handleMailNotification(quickTestArchive, new byte[5], "12345");
        verify(emailService, never()).sendMailToTestedPerson(any(), any());
        verify(emailService, never()).sendMailToHealthDepartment(any(), any());
    }
}