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

package app.coronawarn.quicktest.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class SmsMessage {

    private static final String COUNTRY_CODE_GERMANY = "+49";

    @NotNull
    String endpoint;

    @NotNull
    @Size(min = 1)
    String message;

    public static class SmsMessageBuilder {
        /**
         * Create an SMS endpoint according to E.164 standard.
         * @param endpoint SMS Endpoint, phonenumber in E.164 format (+cc) or local format (017..)
         * @return phonenumber in E.164 format
         */
        public SmsMessageBuilder endpoint(String endpoint) {
            this.endpoint = endpoint.startsWith("0")
                ? endpoint.replaceFirst("0", COUNTRY_CODE_GERMANY) : endpoint;
            return this;
        }
    }
}
