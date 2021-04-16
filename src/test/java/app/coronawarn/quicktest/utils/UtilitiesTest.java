/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2020 - 2021 T-Systems International GmbH and all other contributors
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

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.service.QuickTestServiceException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.mockito.internal.util.collections.Sets;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

@Slf4j
@RequiredArgsConstructor
class UtilitiesTest {

    @Test
    @WithMockUser(username = "myUser", roles = {"myAuthority"})
    public void testGetIdsFromToken() {

        final String tenantIdKey = "tenantId_key_test";
        final String realmKeycloakTenantId = "c19quicktest";

        final String pocIdKey = "poc_id_key_test";
        final String pocKeycloakName = "poc_id";

        final String pocId = "testPOC";

        Map<String, Object> tokens = new HashMap<>();
        tokens.put(tenantIdKey, realmKeycloakTenantId);
        tokens.put(pocIdKey, pocId);

        QuickTestConfig quickTestConfig = new QuickTestConfig();
        quickTestConfig.setPointOfCareIdName(pocKeycloakName);
        quickTestConfig.setTenantIdKey(tenantIdKey);
        quickTestConfig.setTenantPointOfCareIdKey(pocIdKey);

        SecurityContext springSecurityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(springSecurityContext);
        Set<String> roles = Sets.newSet("user");
        KeycloakPrincipal principal = mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext keycloakSecurityContext = mock(RefreshableKeycloakSecurityContext.class);
        when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        when(principal.getKeycloakSecurityContext().getRealm()).thenReturn(realmKeycloakTenantId);
        AccessToken idToken = mock(AccessToken.class);
        when(principal.getKeycloakSecurityContext().getToken()).thenReturn(idToken);
        Map<String, Object> mockTokens = new HashMap<>();
        mockTokens.put(pocKeycloakName, pocId);
        when(idToken.getOtherClaims()).thenReturn(mockTokens);
        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, keycloakSecurityContext);
        KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);
        springSecurityContext.setAuthentication(token);

        try {
            Utilities utilities = new Utilities(quickTestConfig);
            assertEquals(tokens, utilities.getIdsFromToken());
        } catch (QuickTestServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    @WithMockUser(username = "myUser", roles = {"myAuthority"})
    public void testGetIdsFromTokenFailed() {

        final String tenantIdKey = "tenantId_key_test";
        final String realmKeycloakTenantId = "c19quicktest";

        final String pocIdKey = "poc_id_key_test";
        final String pocKeycloakName = "poc_id";

        final String pocId = "testPOC";

        Map<String, Object> tokens = new HashMap<>();
        tokens.put(tenantIdKey, realmKeycloakTenantId);
        tokens.put(pocIdKey, pocId);

        QuickTestConfig quickTestConfig = new QuickTestConfig();
        quickTestConfig.setPointOfCareIdName(pocKeycloakName);
        quickTestConfig.setTenantIdKey(tenantIdKey);
        quickTestConfig.setTenantPointOfCareIdKey(pocIdKey);

        SecurityContext springSecurityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(springSecurityContext);
        Set<String> roles = Sets.newSet("user");
        KeycloakPrincipal principal = mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext keycloakSecurityContext = mock(RefreshableKeycloakSecurityContext.class);
        when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        when(principal.getKeycloakSecurityContext().getRealm()).thenReturn(null);
        AccessToken idToken = mock(AccessToken.class);
        when(principal.getKeycloakSecurityContext().getToken()).thenReturn(idToken);
        Map<String, Object> mockTokens = new HashMap<>();
        when(idToken.getOtherClaims()).thenReturn(mockTokens);
        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, keycloakSecurityContext);
        KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);
        springSecurityContext.setAuthentication(token);

        try {
            Utilities utilities = new Utilities(quickTestConfig);
            utilities.getIdsFromToken();
            fail("No QuickTestServiceException is coming");
        } catch (QuickTestServiceException e) {
            assertEquals(QuickTestServiceException.Reason.INSERT_CONFLICT, e.getReason());
        }
    }
}
