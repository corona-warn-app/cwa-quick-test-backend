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

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_ADMIN;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupDetails;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupId;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupResponse;
import app.coronawarn.quicktest.model.keycloak.KeycloakUserId;
import app.coronawarn.quicktest.service.KeycloakService;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.annotations.IdTokenClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@WebMvcTest(value = GroupManagementController.class, properties = "keycloak-admin.realm=REALM")
@Import({UserManagementControllerUtils.class, Utilities.class})
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class GroupManagementControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    private final static String userId = "user-id";
    private final static String rootGroupId = "0".repeat(36);
    private final static String rootGroupPath = "root-group-path";
    private final static String realmId = "REALM";
    private final static String subGroupId = "a".repeat(36);
    private final static String subGroupPath = "sub-group-path";
    private final static String subGroupName = "sub-group-name";
    private final GroupRepresentation rootGroup = new GroupRepresentation();
    private final GroupRepresentation subGroup = new GroupRepresentation();
    private final UserRepresentation user1 = new UserRepresentation();

    private KeycloakSecurityContext keycloakSecurityContextSpy;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KeycloakService keycloakServiceMock;

    @MockBean
    private Utilities utilities;

    @BeforeEach
    void setup() {
        user1.setId(userId);
        user1.setLastName("lastname");
        user1.setFirstName("firstname");

        subGroup.setName(subGroupName);
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

        KeycloakAuthenticationToken originalAuthenticationToken = (KeycloakAuthenticationToken) originalContext.getAuthentication();
        KeycloakAuthenticationToken authenticationTokenSpy = spy(originalAuthenticationToken);
        when(securityContextSpy.getAuthentication()).thenReturn(authenticationTokenSpy);

        OidcKeycloakAccount originalAccount = originalAuthenticationToken.getAccount();
        OidcKeycloakAccount accountSpy = spy(originalAccount);
        when(authenticationTokenSpy.getAccount()).thenReturn(accountSpy);

        KeycloakSecurityContext originalKeycloakSecurityContext = originalAccount.getKeycloakSecurityContext();
        keycloakSecurityContextSpy = spy(originalKeycloakSecurityContext);
        when(accountSpy.getKeycloakSecurityContext()).thenReturn(keycloakSecurityContextSpy);
        doReturn(realmId).when(keycloakSecurityContextSpy).getRealm();
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetAllGroups() throws Exception {
        KeycloakGroupResponse groupResponse = new KeycloakGroupResponse();
        groupResponse.setId(subGroup.getId());
        groupResponse.setPath(subGroup.getPath());
        groupResponse.setName(subGroup.getName());
        groupResponse.setChildren(Collections.emptyList());

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0]").value(groupResponse));
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetAllGroups_WrongRealm() throws Exception {
        doReturn("randomRealmName").when(keycloakSecurityContextSpy).getRealm();

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetAllGroups_WrongRole() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetAllGroups_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetAllGroups_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetSubgroupDetails() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");
        groupDetails.setName(subGroup.getName());
        groupDetails.setId(subGroupId);

        when(keycloakServiceMock.getSubGroupDetails(subGroupId)).thenReturn(groupDetails);

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(groupDetails));
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetSubgroupDetails_GroupNotFound() throws Exception {
        when(keycloakServiceMock.getSubGroupDetails(subGroupId)).thenReturn(null);

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isNotFound());
    }


    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetSubgroupDetails_GroupNotInSubgroups() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups/randomGroupId"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetSubgroupDetails_WrongRealm() throws Exception {
        doReturn("randomRealmName").when(keycloakSecurityContextSpy).getRealm();

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetSubgroupDetails_WrongRole() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetSubGroupDetails_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetSubgroupDetails_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateSubgroup() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");
        groupDetails.setSearchPortalConsent(false);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isCreated());

        verify(keycloakServiceMock).createGroup(groupDetails,"000000000000000000000000000000000000");
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateSubgroup_ParentNotFound() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");
        groupDetails.setSearchPortalConsent(false);

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND))
            .when(keycloakServiceMock).createGroup(any(),any());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isNotFound());

        verify(keycloakServiceMock).createGroup(groupDetails,"000000000000000000000000000000000000");
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateSubgroup_AlreadyExists() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");
        groupDetails.setSearchPortalConsent(false);

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS))
            .when(keycloakServiceMock).createGroup(any(), any());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isConflict());

        verify(keycloakServiceMock).createGroup(groupDetails,"000000000000000000000000000000000000");
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateSubgroup_ServerError() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");
        groupDetails.setSearchPortalConsent(false);

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR))
            .when(keycloakServiceMock).createGroup(any(), any());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isInternalServerError());

        verify(keycloakServiceMock).createGroup(groupDetails,"000000000000000000000000000000000000");
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateSubgroup_WrongRealm() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");

        doReturn("randomRealmName").when(keycloakSecurityContextSpy).getRealm();

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateSubgroup_WrongRole() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateSubgroup_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());

        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateSubgroup_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateSubgroup() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");
        groupDetails.setSearchPortalConsent(false);

        mockMvc().perform(MockMvcRequestBuilders
                .put("/api/usermanagement/groups/" + subGroupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isNoContent());

        verify(keycloakServiceMock).updateGroup(groupDetails);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateSubgroup_ParentNotFound() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");
        groupDetails.setSearchPortalConsent(false);

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND))
            .when(keycloakServiceMock).updateGroup(any());

        mockMvc().perform(MockMvcRequestBuilders
                .put("/api/usermanagement/groups/" + subGroupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isNotFound());

        verify(keycloakServiceMock).updateGroup(groupDetails);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateSubgroup_ServerError() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");
        groupDetails.setSearchPortalConsent(false);

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR))
            .when(keycloakServiceMock).updateGroup(any());

        mockMvc().perform(MockMvcRequestBuilders
                .put("/api/usermanagement/groups/" + subGroupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isInternalServerError());

        verify(keycloakServiceMock).updateGroup(groupDetails);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateSubgroup_WrongRealm() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");

        doReturn("randomRealmName").when(keycloakSecurityContextSpy).getRealm();

        mockMvc().perform(MockMvcRequestBuilders
                .put("/api/usermanagement/groups/" + subGroupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateSubgroup_WrongRole() throws Exception {
        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");

        mockMvc().perform(MockMvcRequestBuilders
                .put("/api/usermanagement/groups/" + subGroupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateSubgroup_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());

        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");

        mockMvc().perform(MockMvcRequestBuilders
                .put("/api/usermanagement/groups/" + subGroupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateSubgroup_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();
        groupDetails.setName(subGroup.getName());
        groupDetails.setPocDetails("pocDetails");
        groupDetails.setPocId("pocId");

        mockMvc().perform(MockMvcRequestBuilders
                .put("/api/usermanagement/groups/" + subGroupId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupDetails)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping() throws Exception {
        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId(subGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + rootGroupId + "/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isNoContent());

        verify(keycloakServiceMock).moveGroup(subGroupId, rootGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping_SubGroupNotInRootGroup() throws Exception {
        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId("randomGroupId");

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + rootGroupId + "/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isForbidden());

        verify(keycloakServiceMock, never()).moveGroup(any(), any());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping_ParentGroupNotInRootGroup() throws Exception {
        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId(subGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/randomGroupId/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isForbidden());

        verify(keycloakServiceMock, never()).moveGroup(any(), any());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping_ParentNotFound() throws Exception {
        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId(subGroupId);

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND))
            .when(keycloakServiceMock).moveGroup(subGroupId, rootGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + rootGroupId + "/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isNotFound());

        verify(keycloakServiceMock).moveGroup(subGroupId, rootGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping_Conflict() throws Exception {
        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId(subGroupId);

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS))
            .when(keycloakServiceMock).moveGroup(subGroupId, rootGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + rootGroupId + "/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isConflict());

        verify(keycloakServiceMock).moveGroup(subGroupId, rootGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping_ServerError() throws Exception {
        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId(subGroupId);

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR))
            .when(keycloakServiceMock).moveGroup(any(), any());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + rootGroupId + "/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isInternalServerError());

        verify(keycloakServiceMock).moveGroup(subGroupId, rootGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping_WrongRealm() throws Exception {
        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId(subGroupId);

        doReturn("randomRealmName").when(keycloakSecurityContextSpy).getRealm();

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + rootGroupId + "/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping_WrongRole() throws Exception {
        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId(subGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + rootGroupId + "/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());

        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId(subGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + rootGroupId + "/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddSubgroupMapping_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        KeycloakGroupId groupId = new KeycloakGroupId();
        groupId.setGroupId(subGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + rootGroupId + "/subgroups")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(groupId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddUserToGroup() throws Exception {
        KeycloakUserId userId = new KeycloakUserId();
        userId.setUserId(user1.getId());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + subGroupId + "/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isNoContent());

        verify(keycloakServiceMock).moveUser(userId.getUserId(), rootGroupId, subGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddUserToGroup_UserNotInRootGroup() throws Exception {
        KeycloakUserId userId = new KeycloakUserId();
        userId.setUserId("randomUserId");

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + subGroupId + "/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isForbidden());

        verify(keycloakServiceMock, never()).moveUser(userId.getUserId(), rootGroupId, subGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddUserToGroup_GroupNotInRootGroup() throws Exception {
        KeycloakUserId userId = new KeycloakUserId();
        userId.setUserId(user1.getId());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/randomGroupId/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isForbidden());

        verify(keycloakServiceMock, never()).moveUser(userId.getUserId(), rootGroupId, subGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddUserToGroup_ParentNotFound() throws Exception {
        KeycloakUserId userId = new KeycloakUserId();
        userId.setUserId(user1.getId());

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND))
            .when(keycloakServiceMock).moveUser(user1.getId(), rootGroupId, subGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + subGroupId + "/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isNotFound());

        verify(keycloakServiceMock).moveUser(userId.getUserId(), rootGroupId, subGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddUserToGroup_ServerError() throws Exception {
        KeycloakUserId userId = new KeycloakUserId();
        userId.setUserId(user1.getId());

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR))
            .when(keycloakServiceMock).moveUser(any(), any(), any());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/" + subGroupId + "/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isInternalServerError());

        verify(keycloakServiceMock).moveUser(userId.getUserId(), rootGroupId, subGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddUserToGroup_WrongRealm() throws Exception {
        KeycloakUserId userId = new KeycloakUserId();
        userId.setUserId(user1.getId());

        doReturn("randomRealmName").when(keycloakSecurityContextSpy).getRealm();

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/randomGroupId/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddUserToGroup_WrongRole() throws Exception {
        KeycloakUserId userId = new KeycloakUserId();
        userId.setUserId(user1.getId());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/randomGroupId/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddUserToGroup_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());

        KeycloakUserId userId = new KeycloakUserId();
        userId.setUserId(user1.getId());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/randomGroupId/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testAddUserToGroup_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        KeycloakUserId userId = new KeycloakUserId();
        userId.setUserId(user1.getId());

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/usermanagement/groups/randomGroupId/users")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(userId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteGroup() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
                .delete("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isNoContent());

        verify(keycloakServiceMock).deleteGroup(rootGroup.getName(), subGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteGroup_GroupNotInRootGroup() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
                .delete("/api/usermanagement/groups/randomGroupId"))
            .andExpect(status().isForbidden());

        verify(keycloakServiceMock, never()).deleteGroup(rootGroup.getName(), subGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteGroup_GroupNotFound() throws Exception {
        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND))
            .when(keycloakServiceMock).deleteGroup(rootGroup.getName(), subGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .delete("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isNotFound());

        verify(keycloakServiceMock).deleteGroup(rootGroup.getName(), subGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteGroup_ServerError() throws Exception {
        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR))
            .when(keycloakServiceMock).deleteGroup(rootGroup.getName(), subGroupId);

        mockMvc().perform(MockMvcRequestBuilders
                .delete("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isInternalServerError());

        verify(keycloakServiceMock).deleteGroup(rootGroup.getName(), subGroupId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteGroup_WrongRealm() throws Exception {
        doReturn("randomRealmName").when(keycloakSecurityContextSpy).getRealm();

        mockMvc().perform(MockMvcRequestBuilders
                .delete("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteGroup_WrongRole() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
                .delete("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteGroup_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());
        mockMvc().perform(MockMvcRequestBuilders
                .delete("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteGroup_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        mockMvc().perform(MockMvcRequestBuilders
                .delete("/api/usermanagement/groups/" + subGroupId))
            .andExpect(status().isForbidden());
    }
}
