/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2022 T-Systems International GmbH and all other contributors
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
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_TERMINATOR;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.model.cancellation.CancellationRequest;
import app.coronawarn.quicktest.service.KeycloakService;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "keycloak-admin.realm=REALM")
@AutoConfigureMockMvc
@Import({UserManagementControllerUtils.class, Utilities.class})
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class CancellationControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    private final static String partnerId = "TESTID1234";
    private final static String userId = "user-id";
    private final static String rootGroupId = "0".repeat(36);
    private final static String rootGroupPath = "root-group-path";
    private final static String realmId = "REALM";
    private final static String subGroupId = "a".repeat(36);
    private final static String subGroupPath = "sub-group-path";
    private final GroupRepresentation rootGroup = new GroupRepresentation();
    private final GroupRepresentation subGroup = new GroupRepresentation();
    private final UserRepresentation user1 = new UserRepresentation();

    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private KeycloakService keycloakServiceMock;

    @MockBean
    private Utilities utilities;

    @BeforeEach
    void setup() {
        user1.setId(userId);
        user1.setLastName("lastname");
        user1.setFirstName("firstname");

        subGroup.setId(subGroupId);
        subGroup.setPath(subGroupPath);
        rootGroup.setId(rootGroupId);
        rootGroup.setPath(rootGroupPath);
        rootGroup.setSubGroups(List.of(subGroup));
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup));
        when(keycloakServiceMock.getGroupMembers(rootGroupId)).thenReturn(List.of(user1));

        when(utilities.getRootGroupsFromTokenAsList()).thenReturn(List.of(rootGroupId));
        when(keycloakServiceMock.getGroup(rootGroupId)).thenReturn(Optional.of(rootGroup));

        // Inject Realm Name into Security Context
        SecurityContext originalContext = TestSecurityContextHolder.getContext();
        SecurityContext securityContextSpy = spy(originalContext);
        TestSecurityContextHolder.setContext(securityContextSpy);

        KeycloakAuthenticationToken originalAuthenticationToken =
          (KeycloakAuthenticationToken) originalContext.getAuthentication();
        KeycloakAuthenticationToken authenticationTokenSpy = spy(originalAuthenticationToken);
        when(securityContextSpy.getAuthentication()).thenReturn(authenticationTokenSpy);

        OidcKeycloakAccount originalAccount = originalAuthenticationToken.getAccount();
        OidcKeycloakAccount accountSpy = spy(originalAccount);
        when(authenticationTokenSpy.getAccount()).thenReturn(accountSpy);

        KeycloakSecurityContext originalKeycloakSecurityContext = originalAccount.getKeycloakSecurityContext();
        KeycloakSecurityContext keycloakSecurityContextSpy = spy(originalKeycloakSecurityContext);
        when(accountSpy.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContextSpy);
        doReturn(realmId).when(keycloakSecurityContextSpy).getRealm();
    }

    @Test
    @WithMockKeycloakAuth(
      authorities = ROLE_TERMINATOR,
      claims = @OpenIdClaims(sub = userId)
    )
    void createCancellation() throws Exception {
        CancellationRequest request = new CancellationRequest();
        List<String> ids = new ArrayList<>();
        ids.add(partnerId);
        request.setPartnerIds(ids);
        request.setFinalDeletion(LocalDateTime.now());
        String json = mapper.writeValueAsString(request);
        var result = mockMvc().perform(MockMvcRequestBuilders
            .post("/api/cancellation").contentType(MediaType.APPLICATION_JSON)
          .content(json))
          .andExpect(status().isOk()).andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    @WithMockKeycloakAuth(
      authorities = ROLE_LAB,
      claims = @OpenIdClaims(sub = userId)
    )
    void createCancellationWrongRole() throws Exception {
        CancellationRequest request = new CancellationRequest();
        List<String> ids = new ArrayList<>();
        ids.add(partnerId);
        request.setPartnerIds(ids);
        request.setFinalDeletion(LocalDateTime.now());
        String json = mapper.writeValueAsString(request);
        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/cancellation").contentType(MediaType.APPLICATION_JSON)
            .content(json))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
      authorities = ROLE_TERMINATOR,
      claims = @OpenIdClaims(sub = userId)
    )
    void createCancellationEmptyBody() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/cancellation"))
          .andExpect(status().isBadRequest());
    }
}
