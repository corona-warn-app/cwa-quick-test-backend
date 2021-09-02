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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.KeycloakAdminProperties;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.service.KeycloakService;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.GroupRepresentation;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@SpringBootTest
class UtilitiesTest {

    @Autowired
    KeycloakSpringBootProperties keycloakConfig;

    @Autowired
    QuickTestConfig quickTestConfig;

    @Autowired
    KeycloakAdminProperties keycloakAdminProperties;

    @MockBean
    KeycloakService keycloakServiceMock;

    @Autowired
    Utilities utilities;

    @Test
    @WithMockUser(username = "myUser", roles = {"myAuthority"})
    public void testGetIdsFromToken() {
        final String pocId = "testPOC";

        Map<String, Object> tokens = new HashMap<>();
        tokens.put(quickTestConfig.getTenantIdKey(), keycloakConfig.getRealm());
        tokens.put(quickTestConfig.getTenantPointOfCareIdKey(), pocId);

        SecurityContext springSecurityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(springSecurityContext);
        Set<String> roles = Sets.newSet("user");
        KeycloakPrincipal principal = mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext keycloakSecurityContext = mock(RefreshableKeycloakSecurityContext.class);
        when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        when(principal.getKeycloakSecurityContext().getRealm()).thenReturn(keycloakConfig.getRealm());
        AccessToken idToken = mock(AccessToken.class);
        when(principal.getKeycloakSecurityContext().getToken()).thenReturn(idToken);
        Map<String, Object> mockTokens = new HashMap<>();
        mockTokens.put(quickTestConfig.getPointOfCareIdName(), pocId);
        when(idToken.getOtherClaims()).thenReturn(mockTokens);
        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, keycloakSecurityContext);
        KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);
        springSecurityContext.setAuthentication(token);
        assertEquals(tokens, utilities.getIdsFromToken());
    }

    @Test
    @WithMockUser(username = "myUser", roles = {"myAuthority"})
    public void testGetIdsFromTokenForSelfServiceRealm() {
        final String pocId = "testPOC";
        final String userId = "userId";
        final String tokenGroupString = "[" +
                "/rootGroup/NRW/Wuppertal/Barmen," +
                "/rootGroup" +
                "]";
        GroupRepresentation rootGroup = new GroupRepresentation();
        rootGroup.setName("rootGroup");

        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup));

        SecurityContext springSecurityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(springSecurityContext);
        Set<String> roles = Sets.newSet("user");

        KeycloakPrincipal principal = mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext keycloakSecurityContext = mock(RefreshableKeycloakSecurityContext.class);
        when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        when(principal.getKeycloakSecurityContext().getRealm()).thenReturn(keycloakAdminProperties.getRealm());

        AccessToken idToken = mock(AccessToken.class);
        when(idToken.getSubject()).thenReturn(userId);
        when(principal.getKeycloakSecurityContext().getToken()).thenReturn(idToken);
        Map<String, Object> mockTokens = new HashMap<>();
        mockTokens.put(quickTestConfig.getPointOfCareIdName(), pocId);
        mockTokens.put(quickTestConfig.getGroupKey(), tokenGroupString);
        when(idToken.getOtherClaims()).thenReturn(mockTokens);

        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, keycloakSecurityContext);
        KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);
        springSecurityContext.setAuthentication(token);

        Map<String, Object> expectedTokens = new HashMap<>();
        expectedTokens.put(quickTestConfig.getTenantPointOfCareIdKey(), pocId);
        expectedTokens.put(quickTestConfig.getTenantIdKey(), rootGroup.getName());
        assertEquals(expectedTokens, utilities.getIdsFromToken());

    }

    @Test
    @WithMockUser(username = "myUser", roles = {"myAuthority"})
    public void testGetIdsFromTokenFailed() {
        final String pocId = "testPOC";

        Map<String, Object> tokens = new HashMap<>();
        tokens.put(quickTestConfig.getTenantIdKey(), keycloakConfig.getRealm());
        tokens.put(quickTestConfig.getTenantPointOfCareIdKey(), pocId);

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

        ResponseStatusException e = Assertions.assertThrows(ResponseStatusException.class, () -> utilities.getIdsFromToken());
        assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
    }

    @Test
    @WithMockUser(username = "myUser", roles = {"myAuthority"})
    void testGetPocInformationFromToken() {
        final String pocInformation = "name,add,zip,,test";

        SecurityContext springSecurityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(springSecurityContext);
        Set<String> roles = Sets.newSet("user");
        KeycloakPrincipal principal = mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext keycloakSecurityContext = mock(RefreshableKeycloakSecurityContext.class);
        when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        AccessToken idToken = mock(AccessToken.class);
        when(principal.getKeycloakSecurityContext().getToken()).thenReturn(idToken);
        Map<String, Object> mockTokens = new HashMap<>();
        mockTokens.put(quickTestConfig.getPointOfCareInformationName(), pocInformation);
        when(idToken.getOtherClaims()).thenReturn(mockTokens);
        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, keycloakSecurityContext);
        KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);
        springSecurityContext.setAuthentication(token);

        assertEquals(Arrays.asList(pocInformation.split(quickTestConfig.getPointOfCareInformationDelimiter())), utilities.getPocInformationFromToken());
    }

    @Test
    @WithMockUser(username = "myUser", roles = {"myAuthority"})
    void testGetPocInformationFromTokenFailed() {
        final String pocInformation = "name,add,zip,,test";

        SecurityContext springSecurityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(springSecurityContext);
        Set<String> roles = Sets.newSet("user");
        KeycloakPrincipal principal = mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext keycloakSecurityContext = mock(RefreshableKeycloakSecurityContext.class);
        when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        AccessToken idToken = mock(AccessToken.class);
        when(principal.getKeycloakSecurityContext().getToken()).thenReturn(idToken);
        Map<String, Object> mockTokens = new HashMap<>();
        when(idToken.getOtherClaims()).thenReturn(mockTokens);
        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, keycloakSecurityContext);
        KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);
        springSecurityContext.setAuthentication(token);

        ResponseStatusException e = Assertions.assertThrows(ResponseStatusException.class, () -> utilities.getPocInformationFromToken());
        assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
    }

    @Test
    @WithMockUser(username = "myUser", roles = {"myAuthority"})
    void testGetUserNameFromToken() {

        String name = "name";

        SecurityContext springSecurityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(springSecurityContext);
        Set<String> roles = Sets.newSet("user");
        KeycloakPrincipal principal = mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext keycloakSecurityContext = mock(RefreshableKeycloakSecurityContext.class);
        when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        AccessToken idToken = mock(AccessToken.class);
        when(principal.getKeycloakSecurityContext().getToken()).thenReturn(idToken);
        when(idToken.getName()).thenReturn(name);
        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, keycloakSecurityContext);
        KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);
        springSecurityContext.setAuthentication(token);

        assertEquals(name, utilities.getUserNameFromToken());
    }

    @Test
    @WithMockUser(username = "myUser", roles = {"myAuthority"})
    void testGetUserNameFromTokenFailed() {

        SecurityContext springSecurityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(springSecurityContext);
        Set<String> roles = Sets.newSet("user");
        KeycloakPrincipal principal = mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext keycloakSecurityContext = mock(RefreshableKeycloakSecurityContext.class);
        when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        AccessToken idToken = mock(AccessToken.class);
        when(principal.getKeycloakSecurityContext().getToken()).thenReturn(idToken);
        when(idToken.getName()).thenReturn(null);
        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, keycloakSecurityContext);
        KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);
        springSecurityContext.setAuthentication(token);

        ResponseStatusException e = Assertions.assertThrows(ResponseStatusException.class, () -> utilities.getUserNameFromToken());
        assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
    }

    @Test
    void testGetStartTimeForLocalDateInGermanyInUtc() {
        assertEquals(ZonedDateTime.now(ZoneId.of("Europe/Berlin"))
                .with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay()).withZoneSameInstant(ZoneId.of("UTC")),
            Utilities.getStartTimeForLocalDateInGermanyInUtc());
    }

    @Test
    void testGetEndTimeForLocalDateInGermanInUtc() {
        assertEquals(ZonedDateTime.now(ZoneId.of("Europe/Berlin"))
                .with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay()).withZoneSameInstant(ZoneId.of("UTC")),
            Utilities.getEndTimeForLocalDateInGermanyInUtc());
    }

    @Test
    void testGetSubgroupFromToken() {
        final String tokenGroupString = "[" +
          "/rootGroup/NRW/Wuppertal/Barmen," +
          "/rootGroup" +
          "]";

        SecurityContext springSecurityContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(springSecurityContext);
        Set<String> roles = Sets.newSet("user");

        KeycloakPrincipal principal = mock(KeycloakPrincipal.class);
        RefreshableKeycloakSecurityContext keycloakSecurityContext = mock(RefreshableKeycloakSecurityContext.class);
        when(principal.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContext);
        when(principal.getKeycloakSecurityContext().getRealm()).thenReturn(keycloakAdminProperties.getRealm());

        AccessToken idToken = mock(AccessToken.class);
        when(idToken.getSubject()).thenReturn("userId");
        when(principal.getKeycloakSecurityContext().getToken()).thenReturn(idToken);
        Map<String, Object> mockTokens = new HashMap<>();
        mockTokens.put(quickTestConfig.getPointOfCareIdName(), "pocId");
        mockTokens.put(quickTestConfig.getGroupKey(), tokenGroupString);
        when(idToken.getOtherClaims()).thenReturn(mockTokens);

        KeycloakAccount account = new SimpleKeycloakAccount(principal, roles, keycloakSecurityContext);
        KeycloakAuthenticationToken token = new KeycloakAuthenticationToken(account, false);
        springSecurityContext.setAuthentication(token);

        final Optional<String> subGroupFromToken = utilities.getSubGroupFromToken();

        assertTrue(subGroupFromToken.isPresent());
        assertEquals(subGroupFromToken.get(), "Barmen");
    }
}

