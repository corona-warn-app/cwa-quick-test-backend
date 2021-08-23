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

package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.config.KeycloakAdminProperties;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.service.KeycloakService;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class Utilities {

    private final QuickTestConfig quickTestConfig;

    private final KeycloakAdminProperties keycloakAdminProperties;

    private final KeycloakService keycloakService;

    /**
     * Returns current utc datetime.
     */
    public static LocalDateTime getCurrentLocalDateTimeUtc() {
        return ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();
    }

    /**
     * Returns start datetime in Germany.
     */
    public static ZonedDateTime getStartTimeForLocalDateInGermanyInUtc() {
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("Europe/Berlin"))
            .with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay());
        return time.withZoneSameInstant(ZoneId.of("UTC"));
    }

    /**
     * Returns end datetime date in Germany.
     */
    public static ZonedDateTime getEndTimeForLocalDateInGermanyInUtc() {
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("Europe/Berlin"))
            .with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
        return time.withZoneSameInstant(ZoneId.of("UTC"));
    }

    /**
     * Get tenantID and pocID from Token.
     *
     * @return Map with tokens from keycloak (tenantID and pocID)
     * @throws ResponseStatusException 500 if Ids not found in User-Token
     */
    public Map<String, String> getIdsFromToken() throws ResponseStatusException {

        Map<String, String> ids = new HashMap<>();
        Principal principal = getPrincipal();
        long start = System.currentTimeMillis();
        String reqId = UUID.randomUUID().toString();
        log.info("request-uuid:[{}], startGetIds=[{}]", reqId, start);

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            String realmName = keycloakPrincipal.getKeycloakSecurityContext().getRealm();

            if (realmName != null && realmName.equals(keycloakAdminProperties.getRealm())) {
                String rootGroupNames = getRootGroupsFromToken();
                ids.put(quickTestConfig.getTenantIdKey(), rootGroupNames);
            } else {
                ids.put(quickTestConfig.getTenantIdKey(), realmName);
            }
            IDToken token = keycloakPrincipal.getKeycloakSecurityContext().getToken();

            Map<String, Object> customClaims = token.getOtherClaims();

            if (customClaims.containsKey(quickTestConfig.getPointOfCareIdName())) {
                ids.put(quickTestConfig.getTenantPointOfCareIdKey(),
                    String.valueOf(customClaims.get(quickTestConfig.getPointOfCareIdName())));
            }

        }
        if (!ids.containsKey(quickTestConfig.getTenantIdKey())
            || !ids.containsKey(quickTestConfig.getTenantPointOfCareIdKey())) {
            log.warn("Ids not found in User-Token");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ids;
    }

    /**
     * Get root group from Token.
     *
     * @return group
     * @throws ResponseStatusException 500 if Id not found in User-Token
     */
    public String getRootGroupsFromToken() throws ResponseStatusException {

        String information = null;
        Principal principal = getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            IDToken token = keycloakPrincipal.getKeycloakSecurityContext().getToken();
            Map<String, Object> customClaims = token.getOtherClaims();
            if (customClaims.containsKey(quickTestConfig.getGoupKey())) {
                information = String.valueOf(customClaims.get(quickTestConfig.getGoupKey()));
            }
        }
        if (information == null) {
            log.warn("Group not found in User-Token");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Group not found in User-Token");
        }
        List<String> groups = Arrays.asList(information.split(quickTestConfig.getGroupInformationDelimiter()));
        return groups.stream().filter(it -> StringUtils.countOccurrencesOf(it, "/") == 1)
                .map(group -> group.replace("/",""))
                .map(group -> group.replace("]",""))
                .map(group -> group.replace("[",""))
                .collect(Collectors.joining(", "));
    }

    /**
     * Get tenantID and pocID from Token.
     *
     * @return tenantID
     * @throws ResponseStatusException 500 if Id not found in User-Token
     */
    public String getTenantIdFromToken() throws ResponseStatusException {

        Map<String, String> ids = new HashMap<>();
        Principal principal = getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            return keycloakPrincipal.getKeycloakSecurityContext().getRealm();

        }
        log.warn("TenantID not found in User-Token");
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "TenantID not found in User-Token");
    }

    /**
     * Get tenantID and pocID from Token.
     *
     * @return Map with tokens from keycloak (tenantID and pocID)
     * @throws ResponseStatusException 500 if Poc Information not found in User-Token
     */
    public List<String> getPocInformationFromToken() throws ResponseStatusException {

        String information = null;
        Principal principal = getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            IDToken token = keycloakPrincipal.getKeycloakSecurityContext().getToken();
            Map<String, Object> customClaims = token.getOtherClaims();

            if (customClaims.containsKey(quickTestConfig.getPointOfCareInformationName())) {
                information = String.valueOf(customClaims.get(quickTestConfig.getPointOfCareInformationName()));
            }
        }
        if (information == null) {
            log.warn("Poc Information not found in User-Token");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Poc Information not found in User-Token");
        }
        return Arrays.asList(information.split(quickTestConfig.getPointOfCareInformationDelimiter()));
    }

    /**
     * Get tenantID and pocID from Token.
     *
     * @return Map with tokens from keycloak (tenantID and pocID)
     * @throws ResponseStatusException 500 Name not found in User-Token
     */
    public String getUserNameFromToken() throws ResponseStatusException {

        String name = null;
        Principal principal = getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            IDToken token = keycloakPrincipal.getKeycloakSecurityContext().getToken();
            name = token.getName();
        }
        if (name == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Name not found in User-Token");
        }
        return name;
    }

    private Principal getPrincipal() {
        KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken)
            SecurityContextHolder.getContext().getAuthentication();

        return authentication != null ? (Principal) authentication.getPrincipal() : null;
    }
}
