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

import app.coronawarn.quicktest.model.map.MapCenterList;
import app.coronawarn.quicktest.model.map.MapEntryResponse;
import app.coronawarn.quicktest.model.map.MapEntrySingleResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
  name = "MapServerClient",
  url = "${quicktest-map-server.url}",
  configuration = QuicktestMapClientConfig.class
)
public interface QuicktestMapClient {
    String AUTH_TOKEN = "Authorization";

    @PostMapping(value = "/api/centers",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<MapEntryResponse> createOrUpdateMapEntry(@RequestHeader(AUTH_TOKEN) String bearerToken,
                                                  @RequestBody @NotNull @Valid MapCenterList mapCenterList);

    @GetMapping(value = "/api/centers/reference/{userReference}",
      produces = MediaType.APPLICATION_JSON_VALUE
    )
    MapEntrySingleResponse getMapEntry(@RequestHeader(AUTH_TOKEN) String bearerToken,
                                       @PathVariable("userReference") String userReference);

    @DeleteMapping(value = "/api/centers/{centerId}")
    ResponseEntity<Void> deleteMapEntry(@RequestHeader(AUTH_TOKEN) String bearerToken,
                                        @PathVariable("centerId") String centerId);
}
