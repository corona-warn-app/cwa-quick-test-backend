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

package app.coronawarn.quicktest.model.keycloak;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Schema(
  description = "Request/Response model for group details."
)
@Data
public class KeycloakGroupDetails {

    @Schema(description = "Ignored when used for updating the group details")
    private String id;

    @NotEmpty
    @Size(max = 50)
    private String name;

    @Size(max = 300)
    private String pocDetails;

    @Size(max = 50)
    private String pocId;

    private Boolean searchPortalConsent;

    private String website;

    private String email;

    @Size(max = 7)
    private List<@Size(max = 64) String> openingHours;

    private Boolean appointmentRequired;

    // Optional Betriebsstaettennummer of a poc
    @Size(min = 9, max = 9)
    private String bsnr;

    private Boolean enablePcr;
}
