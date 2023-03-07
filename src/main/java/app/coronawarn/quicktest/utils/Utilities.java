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

package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.config.KeycloakAdminProperties;
import app.coronawarn.quicktest.config.QuickTestConfig;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class Utilities {

    private final QuickTestConfig quickTestConfig;

    private final KeycloakAdminProperties keycloakAdminProperties;

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
     * @throws ResponseStatusException 412 if Ids not found in User-Token
     */
    public Map<String, String> getIdsFromToken() throws ResponseStatusException {

        Map<String, String> ids = new HashMap<>();
        Principal principal = getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            String realmName = keycloakPrincipal.getKeycloakSecurityContext().getRealm();

            if (isSharedRealm(realmName)) {
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
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "User has no Group assigned");
        }
        return ids;
    }

    /**
     * Check if Realm is Realm with User Management via QT-Portal or is another shared realm.
     *
     * @param realmName Name of the Realm to check
     * @return if realm is shared.
     */
    private boolean isSharedRealm(String realmName) {
        return realmName != null && (quickTestConfig.getSharedRealms().contains(realmName)
            || keycloakAdminProperties.getRealm().equals(realmName));
    }

    /**
     * Get root group from Token.
     *
     * @return group
     * @throws ResponseStatusException 500 if Id not found in User-Token
     */
    public String getRootGroupsFromToken() throws ResponseStatusException {
        return String.join(", ", getRootGroupsFromTokenAsList());
    }

    /**
     * Get root Groups from token as list.
     * @return List of rootGroup Ids
     * @throws ResponseStatusException 500 if not found in token
     */
    public List<String> getRootGroupsFromTokenAsList() throws ResponseStatusException {
        return getGroupsFromToken().stream().filter(it -> StringUtils.countMatches(it, "/") == 1)
          .map(group -> group.replaceAll("/", ""))
          .map(String::trim)
          .collect(Collectors.toList());
    }

    /**
     * Get the subgroup from token if present.
     * @return Optional of subgroup.
     * @throws ResponseStatusException 500 if not found in token
     */
    public Optional<String> getSubGroupFromToken() throws ResponseStatusException {
        return getGroupsFromToken().stream()
          .filter(it -> StringUtils.countMatches(it, "/") > 1)
          .map(group -> StringUtils.substringAfterLast(group, "/"))
          .map(group -> group.replaceAll("/", ""))
          .map(String::trim)
          .collect(Collectors.reducing((a, b) -> null));
    }

    /**
     * Get tenantID and pocID from Token.
     *
     * @return tenantID
     * @throws ResponseStatusException 500 if Id not found in User-Token
     */
    public String getTenantIdFromToken() throws ResponseStatusException {
        return getIdsFromToken().get(quickTestConfig.getTenantIdKey());
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
            Map<String, Object> customClaims = getCustomClaims((KeycloakPrincipal) principal);

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

    private List<String> getGroupsFromToken() {
        Principal principal = getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            Map<String, Object> customClaims = getCustomClaims((KeycloakPrincipal) principal);
            if (customClaims.containsKey(quickTestConfig.getGroupKey())) {
                Object claim = customClaims.get(quickTestConfig.getGroupKey());

                if (claim instanceof List) {
                    return ((List<Object>) claim).stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                } else {
                    return Collections.singletonList(String.valueOf(claim));
                }
            }
        }

        log.warn("Group not found in User-Token");
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Group not found in User-Token");

    }

    /**
     * Check the Token for pcr_enabled flag.
     */
    public boolean checkPocNatPermission() {
        Principal principal = getPrincipal();
        boolean pcrEnabled = false;
        if (principal instanceof KeycloakPrincipal) {
            Map<String, Object> customClaims = getCustomClaims((KeycloakPrincipal) principal);

            if (customClaims.containsKey(quickTestConfig.getPcrEnabledKey())) {
                pcrEnabled = Boolean.parseBoolean(String.valueOf(customClaims.get(quickTestConfig.getPcrEnabledKey())));
            }
        }
        return pcrEnabled;
    }

    private Map<String, Object> getCustomClaims(KeycloakPrincipal principal) {
        KeycloakPrincipal keycloakPrincipal = principal;
        IDToken token = keycloakPrincipal.getKeycloakSecurityContext().getToken();
        return token.getOtherClaims();
    }

}
