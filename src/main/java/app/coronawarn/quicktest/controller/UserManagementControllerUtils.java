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
import app.coronawarn.quicktest.config.SecurityConfig;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupDetails;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupResponse;
import app.coronawarn.quicktest.service.KeycloakService;
import app.coronawarn.quicktest.utils.Utilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

    private final Utilities utilities;

    protected void checkRealm(KeycloakAuthenticationToken token) throws ResponseStatusException {
        KeycloakSecurityContext securityContext = token.getAccount().getKeycloakSecurityContext();

        if (!config.getRealm().equals(securityContext.getRealm())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User Management is only allowed for main realm");
        }
    }

    protected GroupRepresentation checkUserRootGroup() throws ResponseStatusException {
        List<String> userRootGroups = utilities.getRootGroupsFromTokenAsList();

        if (userRootGroups.size() > 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your user cannot be in more than one root group");
        } else if (userRootGroups.size() < 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your user is not assigned to a root group");
        }
        Optional<GroupRepresentation> group = keycloakService.getGroup(userRootGroups.get(0));

        if (group.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Root GroupRepresentation not found");
        }
        return group.get();
    }

    /**
     * Method to convert list of Keycloak {@link GroupRepresentation}. Method works recursive to transform also
     * sub group items.
     *
     * @param list          output List to add converted items.
     * @param groups        Input Group List
     * @param keepStructure flag whether the generated list should respect the structure of the subgroups. If false all
     *                      groups will be put into 1-dimensional list.
     */
    protected void convertGroups(
        List<KeycloakGroupResponse> list, List<GroupRepresentation> groups, boolean keepStructure) {
        if (groups == null) { // exit condition for recursion
            return;
        }

        groups.forEach(group -> {
            KeycloakGroupResponse response = new KeycloakGroupResponse();
            response.setName(group.getName());
            response.setId(group.getId());
            response.setPath(group.getPath());

            if (keepStructure) {
                convertGroups(response.getChildren(), group.getSubGroups(), true);
            } else {
                convertGroups(list, group.getSubGroups(), false);
            }

            list.add(response);
        });
    }

    /**
     * Checks whether a given group ID is within the subgroups of a given root group.
     *
     * @param rootGroup root group
     * @param groupId   ID of subgroup to check
     * @return The subgroup object.
     * @throws ResponseStatusException if group is not within subgroups.
     */
    protected KeycloakGroupResponse checkGroupIsInSubgroups(
        GroupRepresentation rootGroup, String groupId) throws ResponseStatusException {
        List<KeycloakGroupResponse> groups = new ArrayList<>();
        convertGroups(groups, rootGroup.getSubGroups(), false);
        Map<String, KeycloakGroupResponse> groupsMap = groups.stream()
            .collect(Collectors.toMap(KeycloakGroupResponse::getId, keycloakGroupResponse -> keycloakGroupResponse));

        if (!groupsMap.containsKey(groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Group is not within your subgroups");
        } else {
            return groupsMap.get(groupId);
        }
    }

    protected void checkPermissionsBasedOnInput(KeycloakAuthenticationToken token, KeycloakGroupDetails details)
        throws ResponseStatusException {
        if (details.getEnablePcr() != null && details.getEnablePcr()) {
            boolean hasRolePocNatAdmin = token.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals(SecurityConfig.ROLE_POC_NAT_ADMIN));
            if (!hasRolePocNatAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to enable PocNat");
            }
        }
    }
}
