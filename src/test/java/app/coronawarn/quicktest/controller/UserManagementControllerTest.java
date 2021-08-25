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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.model.keycloak.KeycloakCreateUserRequest;
import app.coronawarn.quicktest.model.keycloak.KeycloakUpdateUserRequest;
import app.coronawarn.quicktest.model.keycloak.KeycloakUserResponse;
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
@WebMvcTest(value = UserManagementController.class, properties = "keycloak-admin.realm=REALM")
@Import({UserManagementControllerUtils.class, Utilities.class})
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class UserManagementControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    private final static String userId = "user-id";
    private final static String rootGroupId = "0".repeat(36);
    private final static String rootGroupPath = "root-group-path";
    private final static String realmId = "REALM";
    private final static String subGroupId = "a".repeat(36);
    private final static String subGroupPath = "sub-group-path";
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
    void testGetAllUsers() throws Exception {
        KeycloakUserResponse userResponse = new KeycloakUserResponse();
        userResponse.setId(user1.getId());
        userResponse.setSubGroup(subGroup.getId());
        userResponse.setLastName(user1.getLastName());
        userResponse.setFirstName(user1.getFirstName());
        userResponse.setRoleCounter(true);
        userResponse.setRoleLab(false);

        when(keycloakServiceMock.getExtendedUserListForRootGroup(rootGroupId)).thenReturn(List.of(userResponse));

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0]").value(userResponse));
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetAllUsers_WrongRealm() throws Exception {
        doReturn("randomRealmName").when(keycloakSecurityContextSpy).getRealm();

        mockMvc().perform(MockMvcRequestBuilders
            .get("/api/usermanagement/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetAllUsers_WrongRole() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
            .get("/api/usermanagement/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetAllUsers_AssignedToNoRootGroup() throws Exception {
        when(utilities.getRootGroupsFromTokenAsList()).thenReturn(Collections.emptyList());

        mockMvc().perform(MockMvcRequestBuilders
            .get("/api/usermanagement/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetAllUsers_AssignedToTwoRootGroups() throws Exception {
        when(utilities.getRootGroupsFromTokenAsList()).thenReturn(List.of(rootGroupId, rootGroupId));

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/users"))
            .andExpect(status().isForbidden());
    }


    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetUserDetails() throws Exception {
        KeycloakUserResponse userResponse = new KeycloakUserResponse();
        userResponse.setId(user1.getId());
        userResponse.setSubGroup(subGroup.getId());
        userResponse.setLastName(user1.getLastName());
        userResponse.setFirstName(user1.getFirstName());
        userResponse.setRoleCounter(true);
        userResponse.setRoleLab(false);

        when(keycloakServiceMock.getUserDetails(userId, rootGroupId)).thenReturn(userResponse);

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/users/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(userResponse));
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetUserDetails_WrongRealm() throws Exception {
        doReturn("randomRealmName").when(keycloakSecurityContextSpy).getRealm();

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/users/userid"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetUserDetails_WrongRole() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/users/userid"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetUserDetails_AssignedToNoRootGroup() throws Exception {
        when(utilities.getRootGroupsFromTokenAsList()).thenReturn(Collections.emptyList());

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/users/userid"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testGetUserDetails_AssignedToTwoRootGroups() throws Exception {
        when(utilities.getRootGroupsFromTokenAsList()).thenReturn(List.of(rootGroupId, rootGroupId));

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/usermanagement/users/userid"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteUser() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
                .delete("/api/usermanagement/users/" + userId))
            .andExpect(status().isNoContent());

        verify(keycloakServiceMock).deleteUser(userId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteUser_UserNotInRootGroup() throws Exception {
        when(keycloakServiceMock.getGroupMembers(rootGroupId)).thenReturn(Collections.emptyList());

        mockMvc().perform(MockMvcRequestBuilders
            .delete("/api/usermanagement/users/" + userId))
            .andExpect(status().isForbidden());

        verify(keycloakServiceMock, never()).deleteUser(userId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteUser_UserNotFound() throws Exception {
        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND))
            .when(keycloakServiceMock).deleteUser(userId);

        mockMvc().perform(MockMvcRequestBuilders
            .delete("/api/usermanagement/users/" + userId))
            .andExpect(status().isNotFound());

        verify(keycloakServiceMock).deleteUser(userId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteUser_ServerError() throws Exception {
        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR))
            .when(keycloakServiceMock).deleteUser(userId);

        mockMvc().perform(MockMvcRequestBuilders
            .delete("/api/usermanagement/users/" + userId))
            .andExpect(status().isInternalServerError());

        verify(keycloakServiceMock).deleteUser(userId);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteUser_WrongRole() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
            .delete("/api/usermanagement/users/" + userId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteUser_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());

        mockMvc().perform(MockMvcRequestBuilders
            .delete("/api/usermanagement/users/" + userId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testDeleteUser_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        mockMvc().perform(MockMvcRequestBuilders
            .delete("/api/usermanagement/users/" + userId))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_Names() throws Exception {
        KeycloakUpdateUserRequest updateUserRequest = new KeycloakUpdateUserRequest();
        updateUserRequest.setFirstName("newFirstName");
        updateUserRequest.setLastName("newLastName");

        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isNoContent());

        verify(keycloakServiceMock).updateUserNames(userId, updateUserRequest.getFirstName(), updateUserRequest.getLastName());
        verify(keycloakServiceMock, never()).updateUserPassword(any(), any());
        verify(keycloakServiceMock, never()).updateUserRoles(any(), anyBoolean(), anyBoolean());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_Password() throws Exception {
        KeycloakUpdateUserRequest updateUserRequest = new KeycloakUpdateUserRequest();
        updateUserRequest.setPassword("newPassword");

        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isNoContent());

        verify(keycloakServiceMock, never()).updateUserNames(any(), any(), any());
        verify(keycloakServiceMock).updateUserPassword(userId, updateUserRequest.getPassword());
        verify(keycloakServiceMock, never()).updateUserRoles(any(), anyBoolean(), anyBoolean());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_Roles() throws Exception {
        KeycloakUpdateUserRequest updateUserRequest = new KeycloakUpdateUserRequest();
        updateUserRequest.setRoleCounter(true);
        updateUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isNoContent());

        verify(keycloakServiceMock, never()).updateUserNames(any(), any(), any());
        verify(keycloakServiceMock, never()).updateUserPassword(any(), any());
        verify(keycloakServiceMock).updateUserRoles(userId, updateUserRequest.getRoleCounter(), updateUserRequest.getRoleLab());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_All() throws Exception {
        KeycloakUpdateUserRequest updateUserRequest = new KeycloakUpdateUserRequest();
        updateUserRequest.setFirstName("newFirstName");
        updateUserRequest.setLastName("newLastName");
        updateUserRequest.setPassword("newPassword");
        updateUserRequest.setRoleCounter(true);
        updateUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isNoContent());

        verify(keycloakServiceMock).updateUserNames(userId, updateUserRequest.getFirstName(), updateUserRequest.getLastName());
        verify(keycloakServiceMock).updateUserPassword(userId, updateUserRequest.getPassword());
        verify(keycloakServiceMock).updateUserRoles(userId, updateUserRequest.getRoleCounter(), updateUserRequest.getRoleLab());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_UserNotInRootGroup() throws Exception {
        when(keycloakServiceMock.getGroupMembers(rootGroupId)).thenReturn(Collections.emptyList());

        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content("{}"))
            .andExpect(status().isForbidden());

        verify(keycloakServiceMock, never()).updateUserPassword(any(), any());
        verify(keycloakServiceMock, never()).updateUserRoles(any(), anyBoolean(), anyBoolean());
        verify(keycloakServiceMock, never()).updateUserNames(any(), any(), any());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_UserNotFound() throws Exception {

        KeycloakUpdateUserRequest updateUserRequest = new KeycloakUpdateUserRequest();
        updateUserRequest.setFirstName("newFirstName");
        updateUserRequest.setLastName("newLastName");

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND))
            .when(keycloakServiceMock).updateUserNames(any(), any(), any());

        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isNotFound());

        verify(keycloakServiceMock).updateUserNames(userId, updateUserRequest.getFirstName(), updateUserRequest.getLastName());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_ServerError() throws Exception {
        KeycloakUpdateUserRequest updateUserRequest = new KeycloakUpdateUserRequest();
        updateUserRequest.setFirstName("newFirstName");
        updateUserRequest.setLastName("newLastName");

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR))
            .when(keycloakServiceMock).updateUserNames(any(), any(), any());

        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isInternalServerError());

        verify(keycloakServiceMock).updateUserNames(userId, updateUserRequest.getFirstName(), updateUserRequest.getLastName());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_WrongRole() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());

        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testUpdateUser_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        mockMvc().perform(MockMvcRequestBuilders
            .patch("/api/usermanagement/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateUser() throws Exception {
        KeycloakCreateUserRequest createUserRequest = new KeycloakCreateUserRequest();
        createUserRequest.setSubgroup(subGroup.getId());
        createUserRequest.setUsername("newUsername");
        createUserRequest.setFirstName("newFirstName");
        createUserRequest.setLastName("newLastName");
        createUserRequest.setPassword("newPassword");
        createUserRequest.setRoleCounter(true);
        createUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/usermanagement/users")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(createUserRequest)))
            .andExpect(status().isCreated());

        verify(keycloakServiceMock).createNewUserInGroup(
            createUserRequest.getFirstName(),
            createUserRequest.getLastName(),
            createUserRequest.getUsername(),
            createUserRequest.getPassword(),
            createUserRequest.getRoleCounter(),
            createUserRequest.getRoleLab(),
            rootGroup.getPath(),
            subGroup.getPath());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateUser_WithoutSubgroup() throws Exception {
        KeycloakCreateUserRequest createUserRequest = new KeycloakCreateUserRequest();
        createUserRequest.setSubgroup(null);
        createUserRequest.setUsername("newUsername");
        createUserRequest.setFirstName("newFirstName");
        createUserRequest.setLastName("newLastName");
        createUserRequest.setPassword("newPassword");
        createUserRequest.setRoleCounter(true);
        createUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/usermanagement/users")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(createUserRequest)))
            .andExpect(status().isCreated());

        verify(keycloakServiceMock).createNewUserInGroup(
            createUserRequest.getFirstName(),
            createUserRequest.getLastName(),
            createUserRequest.getUsername(),
            createUserRequest.getPassword(),
            createUserRequest.getRoleCounter(),
            createUserRequest.getRoleLab(),
            rootGroup.getPath(),
            null);
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateUser_SubGroupNotInRootGroup() throws Exception {
        KeycloakCreateUserRequest createUserRequest = new KeycloakCreateUserRequest();
        createUserRequest.setSubgroup("x".repeat(36));
        createUserRequest.setUsername("newUsername");
        createUserRequest.setFirstName("newFirstName");
        createUserRequest.setLastName("newLastName");
        createUserRequest.setPassword("newPassword");
        createUserRequest.setRoleCounter(true);
        createUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/usermanagement/users")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(createUserRequest)))
            .andExpect(status().isForbidden());

        verify(keycloakServiceMock, never())
            .createNewUserInGroup(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any(), any());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateUser_UserAlreadyExists() throws Exception {

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS))
            .when(keycloakServiceMock).createNewUserInGroup(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any(), any());

        KeycloakCreateUserRequest createUserRequest = new KeycloakCreateUserRequest();
        createUserRequest.setSubgroup(subGroup.getId());
        createUserRequest.setUsername("newUsername");
        createUserRequest.setFirstName("newFirstName");
        createUserRequest.setLastName("newLastName");
        createUserRequest.setPassword("newPassword");
        createUserRequest.setRoleCounter(true);
        createUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/usermanagement/users")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(createUserRequest)))
            .andExpect(status().isConflict());

        verify(keycloakServiceMock).createNewUserInGroup(
            createUserRequest.getFirstName(),
            createUserRequest.getLastName(),
            createUserRequest.getUsername(),
            createUserRequest.getPassword(),
            createUserRequest.getRoleCounter(),
            createUserRequest.getRoleLab(),
            rootGroup.getPath(),
            subGroup.getPath());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateUser_BadRequest() throws Exception {

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.BAD_REQUEST))
            .when(keycloakServiceMock).createNewUserInGroup(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any(), any());

        KeycloakCreateUserRequest createUserRequest = new KeycloakCreateUserRequest();
        createUserRequest.setSubgroup(subGroup.getId());
        createUserRequest.setUsername("newUsername");
        createUserRequest.setFirstName("newFirstName");
        createUserRequest.setLastName("newLastName");
        createUserRequest.setPassword("newPassword");
        createUserRequest.setRoleCounter(true);
        createUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/usermanagement/users")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(createUserRequest)))
            .andExpect(status().isBadRequest());

        verify(keycloakServiceMock).createNewUserInGroup(
            createUserRequest.getFirstName(),
            createUserRequest.getLastName(),
            createUserRequest.getUsername(),
            createUserRequest.getPassword(),
            createUserRequest.getRoleCounter(),
            createUserRequest.getRoleLab(),
            rootGroup.getPath(),
            subGroup.getPath());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ADMIN,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateUser_ServerError() throws Exception {

        doThrow(new KeycloakService.KeycloakServiceException(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR))
            .when(keycloakServiceMock).createNewUserInGroup(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any(), any());

        KeycloakCreateUserRequest createUserRequest = new KeycloakCreateUserRequest();
        createUserRequest.setSubgroup(subGroup.getId());
        createUserRequest.setUsername("newUsername");
        createUserRequest.setFirstName("newFirstName");
        createUserRequest.setLastName("newLastName");
        createUserRequest.setPassword("newPassword");
        createUserRequest.setRoleCounter(true);
        createUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/usermanagement/users")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(createUserRequest)))
            .andExpect(status().isInternalServerError());

        verify(keycloakServiceMock).createNewUserInGroup(
            createUserRequest.getFirstName(),
            createUserRequest.getLastName(),
            createUserRequest.getUsername(),
            createUserRequest.getPassword(),
            createUserRequest.getRoleCounter(),
            createUserRequest.getRoleLab(),
            rootGroup.getPath(),
            subGroup.getPath());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateUser_WrongRole() throws Exception {
        KeycloakCreateUserRequest createUserRequest = new KeycloakCreateUserRequest();
        createUserRequest.setSubgroup(subGroup.getId());
        createUserRequest.setUsername("newUsername");
        createUserRequest.setFirstName("newFirstName");
        createUserRequest.setLastName("newLastName");
        createUserRequest.setPassword("newPassword");
        createUserRequest.setRoleCounter(true);
        createUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/usermanagement/users")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(createUserRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateUser_AssignedToNoRootGroup() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(Collections.emptyList());

        KeycloakCreateUserRequest createUserRequest = new KeycloakCreateUserRequest();
        createUserRequest.setSubgroup(subGroup.getId());
        createUserRequest.setUsername("newUsername");
        createUserRequest.setFirstName("newFirstName");
        createUserRequest.setLastName("newLastName");
        createUserRequest.setPassword("newPassword");
        createUserRequest.setRoleCounter(true);
        createUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/usermanagement/users")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(createUserRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_LAB,
        id = @IdTokenClaims(sub = userId)
    )
    void testCreateUser_AssignedToTwoRootGroups() throws Exception {
        when(keycloakServiceMock.getRootGroupsOfUser(userId)).thenReturn(List.of(rootGroup, rootGroup));

        KeycloakCreateUserRequest createUserRequest = new KeycloakCreateUserRequest();
        createUserRequest.setSubgroup(subGroup.getId());
        createUserRequest.setUsername("newUsername");
        createUserRequest.setFirstName("newFirstName");
        createUserRequest.setLastName("newLastName");
        createUserRequest.setPassword("newPassword");
        createUserRequest.setRoleCounter(true);
        createUserRequest.setRoleLab(true);

        mockMvc().perform(MockMvcRequestBuilders
            .post("/api/usermanagement/users")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(createUserRequest)))
            .andExpect(status().isForbidden());
    }
}
