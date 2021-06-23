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
import app.coronawarn.quicktest.model.SmsMessage;
import com.huawei.openstack4j.api.OSClient;
import com.huawei.openstack4j.api.exceptions.AuthenticationException;
import com.huawei.openstack4j.core.transport.Config;
import com.huawei.openstack4j.model.common.Identifier;
import com.huawei.openstack4j.model.identity.v3.Token;
import com.huawei.openstack4j.openstack.OSFactory;
import com.huawei.openstack4j.openstack.message.notification.domain.MessageIdResponse;
import org.springframework.stereotype.Component;

@Component
public class OsFactoryWrapper {

    /**
     * Wrapping the static client creation.
     * @param config Overridden Endpoint
     * @param otcAuth authentication information
     * @return Authentication token
     * @throws AuthenticationException thrown if authentication fails
     */
    public Token authenticate(Config config, SmsConfig.OtcAuth otcAuth) throws AuthenticationException {
        OSClient.OSClientV3 openstackClient = OSFactory.builderV3().withConfig(config)
          .endpoint(otcAuth.getAuthUrl())
          .credentials(otcAuth.getUser(), otcAuth.getPassword(), Identifier.byId(otcAuth.getDomainId()))
          .scopeToProject(Identifier.byId(otcAuth.getProjectId()))
          .authenticate();
        return openstackClient.getToken();
    }

    public void refreshToken() {
        OSFactory.refreshToken();
    }

    /**
     * Wrapping the static Notification sms send call.
     *
     * @param token Authentication token
     * @param config Overriden Endpoint
     * @param sms Message to be sent
     * @return messageId and requestId
     */
    public MessageIdResponse sendSms(Token token, Config config, SmsMessage sms) {
        // Get client from token to enable multithreaded sessions and prevent the necessity of reauthentication
        // http://www.openstack4j.com/learn/threads/
        return OSFactory.clientFromToken(token).withConfig(config)
          .notification().sms()
          .send(sms.getEndpoint(), sms.getMessage(), null);
    }
}
