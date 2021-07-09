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

package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.model.KeyCloakConfigFile;
import app.coronawarn.quicktest.model.QuickTestContextFile;
import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final KeycloakSpringBootProperties keycloakConfig;

    private final QuickTestConfig quickTestConfig;

    @GetMapping(value = "keycloak.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyCloakConfigFile> getKeyCloakConfig() {
        return ResponseEntity.ok(
            new KeyCloakConfigFile(keycloakConfig.getAuthServerUrl()));
    }

    @GetMapping(value = "context.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuickTestContextFile> getQuickTestContextFile() {
        return ResponseEntity.ok(
            new QuickTestContextFile(quickTestConfig.getFrontendContextConfig().getRulesServerUrl()));
    }
}
