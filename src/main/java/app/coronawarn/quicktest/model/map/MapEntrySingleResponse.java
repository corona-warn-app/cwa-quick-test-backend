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

package app.coronawarn.quicktest.model.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MapEntrySingleResponse {
    @JsonProperty("UUID")
    String uuid;
    @JsonProperty("Name")
    String name;
    @JsonProperty("Website")
    String website;
    @JsonProperty("Longitude")
    Integer longitude;
    @JsonProperty("Latitude")
    Integer latitude;
    @JsonProperty("Address")
    String address;
    @JsonProperty("OpeningHours")
    String[] openingHours;
    @JsonProperty("AddressNote")
    String addressNote;
    @JsonProperty("Appointment")
    String appointment;
    @JsonProperty("TestKinds")
    String[] testKinds;
    @JsonProperty("DCC")
    Boolean dcc;
    @JsonProperty("Message")
    String message;
    @JsonProperty("UserReference")
    String userReference;
    @JsonProperty("EnterDate")
    String enterDate;
    @JsonProperty("LeaveDate")
    String leaveDate;
    @JsonProperty("Email")
    String email;
}
