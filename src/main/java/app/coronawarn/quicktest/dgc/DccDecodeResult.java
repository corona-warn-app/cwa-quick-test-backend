/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
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

package app.coronawarn.quicktest.dgc;

import com.fasterxml.jackson.databind.JsonNode;

public class DccDecodeResult {
    private String issuer;
    private long issuedAt;
    private long expiration;
    private String ci;
    private String dccJsonString;
    private JsonNode dccJsonNode;

    public void setCi(String ci) {
        this.ci = ci;
    }

    public void setDccJsonString(String dccJsonString) {
        this.dccJsonString = dccJsonString;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public long getExpiration() {
        return expiration;
    }

    public String getCi() {
        return ci;
    }

    public String getDccJsonString() {
        return dccJsonString;
    }

    public void setDccJsonNode(JsonNode dccElem) {
        this.dccJsonNode = dccElem;
    }

    public JsonNode getDccJsonNode() {
        return dccJsonNode;
    }
}
