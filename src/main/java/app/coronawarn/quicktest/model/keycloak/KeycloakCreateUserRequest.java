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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(
  description = "Request model for user creation."
)
@Data
public class KeycloakCreateUserRequest {

    @NotEmpty
    @Size(max = 30)
    private String lastName;

    @NotEmpty
    @Size(max = 30)
    private String firstName;

    @NotEmpty
    @Size(max = 50)
    private String username;

    @NotEmpty
    @Size(min = 8, max = 64)
    private String password;

    @Size(min = 36, max = 36)
    @Schema(description = "ID of a subgroup to put user into. If not provided user will just put into root group.")
    private String subGroup;

    @NotNull
    private Boolean roleCounter;

    @NotNull
    private Boolean roleLab;

}
