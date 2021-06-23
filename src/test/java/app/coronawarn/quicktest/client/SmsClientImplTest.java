package app.coronawarn.quicktest.client;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.SmsConfig;
import app.coronawarn.quicktest.exception.SmsException;
import app.coronawarn.quicktest.model.SmsMessage;
import app.coronawarn.quicktest.model.SmsResponse;
import com.huawei.openstack4j.api.exceptions.AuthenticationException;
import com.huawei.openstack4j.api.exceptions.ClientResponseException;
import com.huawei.openstack4j.api.exceptions.ConnectionException;
import com.huawei.openstack4j.api.exceptions.OS4JException;
import com.huawei.openstack4j.api.exceptions.ServerResponseException;
import com.huawei.openstack4j.api.types.ServiceType;
import com.huawei.openstack4j.core.transport.Config;
import com.huawei.openstack4j.openstack.identity.internal.OverridableEndpointURLResolver;
import com.huawei.openstack4j.openstack.message.notification.domain.MessageIdResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SmsClientImplTest {

    @InjectMocks
    SmsClientImpl underTest;

    @Mock
    SmsConfig smsConfig;

    @Mock
    OsFactoryWrapper osFactoryWrapper;


    @Test
    void setEndpointAndAuthenticateIfEnabled()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String smsEndpoint = "smsEndpoint";
        SmsConfig.OtcAuth otcAuth = new SmsConfig.OtcAuth();
        otcAuth.setNotificationEndpoint(smsEndpoint);
        when(smsConfig.isEnabled()).thenReturn(true);
        when(smsConfig.getOtcAuth()).thenReturn(otcAuth);
        initialize();

        ArgumentCaptor<Config> configArgumentCaptor = ArgumentCaptor.forClass(Config.class);
        verify(osFactoryWrapper).authenticate(configArgumentCaptor.capture(), any());
        OverridableEndpointURLResolver customEndpoints =
          (OverridableEndpointURLResolver) configArgumentCaptor.getValue().getEndpointURLResolver();
        assertThat(customEndpoints.getOverrides().containsKey(ServiceType.Notification)).isTrue();
        assertThat(customEndpoints.getOverrides().get(ServiceType.Notification)).isEqualTo(smsEndpoint);
    }

    @Test
    void skipAuthenticationIfDisabled()
      throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        when(smsConfig.isEnabled()).thenReturn(false);
        initialize();

        verifyNoInteractions(osFactoryWrapper);
    }

    @Test
    void sendSmsSuccessful() throws SmsException {
        String messageId = "msg";
        String receiver = "01761234567";
        String messageText = "this is a text";
        SmsMessage sms = SmsMessage.builder().message(messageText).endpoint(receiver).build();
        when(osFactoryWrapper.sendSms(any(), any(), any())).thenReturn(new MessageIdResponse(messageId));
        SmsResponse response = underTest.send(sms);

        assertThat(response.getMessageId()).isEqualTo(messageId);
    }

    @Test
    void sendSmsNotAuthenticated() {
        when(osFactoryWrapper.sendSms(any(), any(), any()))
          .thenThrow(new AuthenticationException("Wrong password", 401));

        SmsException ex = assertThrows(SmsException.class, () -> underTest.send(SmsMessage.builder().build()));
        assertThat(ex.getReason()).isEqualTo(SmsException.Reason.SEND_SMS_FAILED);
        verify(osFactoryWrapper).refreshToken();
    }

    @ParameterizedTest
    @MethodSource("os4jToReason")
    void sendSmsExceptions(OS4JException source, SmsException.Reason target) {
        when(osFactoryWrapper.sendSms(any(), any(), any())).thenThrow(source);

        SmsException exception = assertThrows(SmsException.class, () -> underTest.send(SmsMessage.builder().build()));
        assertThat(exception.getReason()).isEqualTo(target);
    }

    private static Stream<Arguments> os4jToReason() {
        return Stream.of(
          arguments(new ConnectionException("conn", 504, new Throwable()), SmsException.Reason.COULD_NOT_REACH_HOST),
          arguments(new ClientResponseException("Bad input", 400), SmsException.Reason.WRONG_INPUT),
          arguments(new ServerResponseException("Internal", 500), SmsException.Reason.SERVER_FAILURE),
          arguments(new OS4JException("Error"), SmsException.Reason.SEND_SMS_FAILED)
        );
    }

    private void initialize() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = SmsClientImpl.class.getDeclaredMethod("initialize");
        method.setAccessible(true);
        SmsClientImpl smsClient = new SmsClientImpl(smsConfig, osFactoryWrapper);
        method.invoke(smsClient);
    }
}