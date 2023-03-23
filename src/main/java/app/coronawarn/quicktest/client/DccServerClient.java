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

package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.dcc.DccPublicKey;
import app.coronawarn.quicktest.model.dcc.DccUploadData;
import app.coronawarn.quicktest.model.dcc.DccUploadResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
  name = "dccServerClient",
  url = "${cwa-dcc-server.url}",
  configuration = DccServerClientConfig.class
)
public interface DccServerClient {
    @GetMapping(value = "/version/v1/publicKey/search/{labId}",
      consumes = MediaType.APPLICATION_JSON_VALUE
    )
    List<DccPublicKey> searchPublicKeys(@PathVariable("labId") String labId);

    @PostMapping(value = "/version/v1/test/{testId}/dcc",
      consumes = MediaType.APPLICATION_JSON_VALUE
    )
    DccUploadResult uploadDcc(@PathVariable("testId") String testId,
                              @RequestBody @NotNull @Valid DccUploadData dccUploadData);


}
