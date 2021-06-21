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

package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.config.SmsConfig;
import app.coronawarn.quicktest.exception.SmsException;
import app.coronawarn.quicktest.model.SmsMessage;
import app.coronawarn.quicktest.model.SmsResponse;
import com.huawei.openstack4j.api.OSClient;
import com.huawei.openstack4j.api.exceptions.AuthenticationException;
import com.huawei.openstack4j.api.exceptions.ClientResponseException;
import com.huawei.openstack4j.api.exceptions.ConnectionException;
import com.huawei.openstack4j.api.exceptions.OS4JException;
import com.huawei.openstack4j.api.exceptions.ServerResponseException;
import com.huawei.openstack4j.api.types.ServiceType;
import com.huawei.openstack4j.core.transport.Config;
import com.huawei.openstack4j.model.common.Identifier;
import com.huawei.openstack4j.model.identity.v3.Token;
import com.huawei.openstack4j.openstack.OSFactory;
import com.huawei.openstack4j.openstack.identity.internal.OverridableEndpointURLResolver;
import com.huawei.openstack4j.openstack.message.notification.domain.MessageIdResponse;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsClientImpl implements SmsClient {

    private final SmsConfig smsConfig;

    private Token token;
    private Config openstackConfig;

    @PostConstruct
    private void initialize() {
        if (smsConfig.isEnabled()) {
            SmsConfig.OtcAuth otcAuth = smsConfig.getOtcAuth();

            OverridableEndpointURLResolver endpointResolver = new OverridableEndpointURLResolver();
            endpointResolver.addOverrideEndpoint(ServiceType.Notification, otcAuth.getNotificationEndpoint());
            this.openstackConfig = Config.newConfig().withEndpointURLResolver(endpointResolver);

            authenticate(otcAuth);
        }
    }

    @Override
    public SmsResponse send(SmsMessage sms) throws SmsException {
        try {
            MessageIdResponse messageIdResponse = OSFactory.clientFromToken(this.token).withConfig(this.openstackConfig)
              .notification().sms()
              .send(sms.getEndpoint(), sms.getMessage(), null);

            return new SmsResponse(messageIdResponse.getRequestId(), messageIdResponse.getId());
        } catch (AuthenticationException authException) {
            log.warn("Client is not authenticated, retrying authentication: {}",
              authException.getLocalizedMessage());
            reAuthenticate();
            //TODO: retry send?
            throw new SmsException(SmsException.Reason.SEND_SMS_FAILED);
        } catch (ConnectionException connectionException) {
            log.warn("Could not reach SMS OTC host: {}", connectionException.getLocalizedMessage());
            throw new SmsException(SmsException.Reason.COULD_NOT_REACH_HOST);
        } catch (ClientResponseException clientException) {
            log.warn("Error sending SMS due to client input: {}", clientException.getLocalizedMessage());
            throw new SmsException(SmsException.Reason.WRONG_INPUT);
        } catch (ServerResponseException serverException) {
            log.warn("Error sending SMS due to server failure: {}", serverException.getLocalizedMessage());
            throw new SmsException(SmsException.Reason.SERVER_FAILURE);
        } catch (OS4JException e) {
            log.warn("Could not send SMS: {}", e.getLocalizedMessage());
            throw new SmsException(SmsException.Reason.SEND_SMS_FAILED);
        }
    }

    private void authenticate(SmsConfig.OtcAuth otcAuth) {
        try {
            OSClient.OSClientV3 openstackClient = OSFactory.builderV3().withConfig(openstackConfig)
              .endpoint(otcAuth.getAuthUrl())
              .credentials(otcAuth.getUser(), otcAuth.getPassword(), Identifier.byId(otcAuth.getDomainId()))
              .scopeToProject(Identifier.byId(otcAuth.getProjectId()))
              .authenticate();
            this.token = openstackClient.getToken();
        } catch (AuthenticationException e) {
            log.error("Could not authenticate at OTC IAM: {} ", e.getLocalizedMessage());
        }
    }

    private void reAuthenticate() {
        try {
            OSFactory.refreshToken();
        } catch (AuthenticationException e) {
            log.error("Could not authenticate at OTC IAM: {} ", e.getLocalizedMessage());
        }
    }
}
