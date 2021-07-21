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

import app.coronawarn.quicktest.config.KeycloakAdminProperties;
import app.coronawarn.quicktest.service.KeycloakService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserManagementControllerUtils {

    private final KeycloakAdminProperties config;

    private final KeycloakService keycloakService;

    protected void checkRealm(KeycloakAuthenticationToken token) throws ResponseStatusException {
        KeycloakSecurityContext securityContext = token.getAccount().getKeycloakSecurityContext();

        if (!config.getRealm().equals(securityContext.getRealm())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User Management is only allowed for main realm");
        }
    }

    protected GroupRepresentation checkUserRootGroup(KeycloakAuthenticationToken token) throws ResponseStatusException {
        String userId = token.getAccount().getKeycloakSecurityContext().getToken().getSubject();
        List<GroupRepresentation> userRootGroups = keycloakService.getRootGroupsOfUser(userId);

        if (userRootGroups.size() > 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your user cannot be in more than one root group");
        } else if (userRootGroups.size() < 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your user is not assigned to a root group");
        }

        return userRootGroups.get(0);
    }
}
