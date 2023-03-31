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

package app.coronawarn.quicktest.model.cancellation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Schema(
    description = "The ZIP request model to create zip file for partners."
)
@Data
public class ZipRequest {

    @Length(min = 10, max = 100)
    private String password;

    @Length(min = 1, max = 50)
    private String partnerId;

    @Size(min = 1, max = 20)
    private List<@Length(min = 1, max = 100) String> partnerIds;

}
