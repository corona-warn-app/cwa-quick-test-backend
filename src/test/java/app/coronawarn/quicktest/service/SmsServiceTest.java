package app.coronawarn.quicktest.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.client.SmsClient;
import app.coronawarn.quicktest.config.SmsConfig;
import app.coronawarn.quicktest.model.SmsMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    private final static int PASSWORD_LENGTH = 6;

    @InjectMocks
    private SmsService underTest;

    @Mock
    private SmsClient smsClient;
    @Mock
    private SmsConfig smsConfig;

    @Captor
    ArgumentCaptor<SmsMessage> smsMessageCaptor;

    @Test
    void createPassword() {
        when(smsConfig.getPasswordLength()).thenReturn(PASSWORD_LENGTH);
        String password = underTest.createPassword();
        assertThat(password.length()).isEqualTo(PASSWORD_LENGTH);
        assertThat((password).matches("\\d+")).isTrue();
    }

    @Test
    void createsRandomPasswords() {
        when(smsConfig.getPasswordLength()).thenReturn(PASSWORD_LENGTH);
        String password1 = underTest.createPassword();
        String password2 = underTest.createPassword();

        assertThat(password1).isNotEqualTo(password2);
    }

    @Test
    void sendMessageDisabled() {
        when(smsConfig.isEnabled()).thenReturn(false);
        underTest.sendPasswordSms("+491761234567", "123456");
        verify(smsClient, never()).publishSms(any());
    }

    @Test
    void sendMessageWithPassword() {
        when(smsConfig.getPasswordLength()).thenReturn(PASSWORD_LENGTH);
        when(smsConfig.isEnabled()).thenReturn(true);
        when(smsConfig.getMessageTemplate()).thenReturn("Nachricht: %s");
        String receiver = "+491761234567";
        String password = underTest.createPassword();

        underTest.sendPasswordSms(receiver, password);

        verify(smsClient).publishSms(smsMessageCaptor.capture());
        SmsMessage sentMessage = smsMessageCaptor.getValue();
        assertThat(sentMessage.getEndpoint()).isEqualTo(receiver);
        assertThat(sentMessage.getMessage()).endsWith(password);
    }

    @Test
    void sendMessageWithPasswordToNonE164Phonenumber() {
        when(smsConfig.getPasswordLength()).thenReturn(PASSWORD_LENGTH);
        when(smsConfig.isEnabled()).thenReturn(true);
        when(smsConfig.getMessageTemplate()).thenReturn("Nachricht: %s");

        underTest.sendPasswordSms("01761234567", underTest.createPassword());

        verify(smsClient).publishSms(smsMessageCaptor.capture());
        SmsMessage sentMessage = smsMessageCaptor.getValue();
        assertThat(sentMessage.getEndpoint()).isEqualTo("+491761234567");
    }
}