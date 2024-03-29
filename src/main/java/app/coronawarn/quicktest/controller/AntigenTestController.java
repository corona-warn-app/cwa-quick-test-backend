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

package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;

import app.coronawarn.quicktest.domain.AntigenTest;
import app.coronawarn.quicktest.service.AntigenTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(value = "/api/antigentests")
@RequiredArgsConstructor
public class AntigenTestController {

    private final AntigenTestService antigenTestService;

    /**
     * Endpoint for receiving antigen tests.
     *
     * @return AntigenTests
     */
    @Operation(
            summary = "Response antigen tests as JSON array",
            description = ""
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "antigen tests found"),
      @ApiResponse(responseCode = "404", description = "antigen tests empty"),
      @ApiResponse(responseCode = "500", description = "Request failed due to an internal server error")})
    @RequestMapping(path = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_LAB)
    public ResponseEntity<List<AntigenTest>> getAntigenTests() {
        try {
            return ResponseEntity.ok().lastModified(antigenTestService.getLastUpdate().atZone(ZoneId.of("UTC")))
                    .body(antigenTestService.getAntigenTests());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Couldn't prepare antigen tests response.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
