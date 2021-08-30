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

package app.coronawarn.quicktest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.model.keycloak.KeycloakGroupDetails;
import app.coronawarn.quicktest.model.keycloak.KeycloakUserResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

@Slf4j
@SpringBootTest(properties = "keycloak-admin.realm=REALM")
public class KeycloakServiceTest {

    @MockBean
    Keycloak keycloakAdminMock;

    @Autowired
    KeycloakService keycloakService;

    private final String username = "USERNAME";
    private final String firstname = "FIRSTNAME";
    private final String lastname = "LASTNAME";
    private final String password = "PASSWORD";
    private final String userid = "ID";
    private UserRepresentation userRepresentation;
    private UsersResource usersResourceMock;
    private UserResource userResourceMock;
    private RoleMappingResource roleMappingResourceMock;
    private RoleScopeResource roleScopeResourceMock;

    private final String rootGroupname = "Root Group A";
    private final String rootGroupid = "rootgroupid";
    private GroupResource rootGroupResourceMock;
    private GroupRepresentation rootGroupRepresentation;


    private final String groupname = "Group A";
    private final String groupid = "groupid";
    private final String groupPocId = "42";
    private final String groupPocDetails = "Awesome POC";
    private GroupsResource groupsResourceMock;
    private GroupResource groupResourceMock;
    private GroupRepresentation groupRepresentation;

    private final String rootGroupPath = "/Partner R";
    private final String subGroupPath = "/Partner R/SubGroup/POC A";

    private final String realm = "REALM";
    private RealmResource realmResourceMock;

    private final String roleCounterName = "c19_quick_test_counter";
    private final String roleCounterId = "roleIdCounter";
    private RoleResource roleCounterResourceMock;
    private RoleRepresentation roleCounter;
    private final String roleLabName = "c19_quick_test_lab";
    private final String roleLabId = "roleIdLab";
    private RoleResource roleLabResourceMock;
    private RoleRepresentation roleLab;
    private RolesResource rolesResourceMock;

    @BeforeEach
    void setupMocks() {
        // ROLE
        rolesResourceMock = mock(RolesResource.class);
        roleCounter = new RoleRepresentation();
        roleCounter.setId(roleCounterId);
        roleCounter.setName(roleCounterName);
        roleCounterResourceMock = mock(RoleResource.class);
        when(roleCounterResourceMock.toRepresentation()).thenReturn(roleCounter);
        when(rolesResourceMock.get(roleCounterName)).thenReturn(roleCounterResourceMock);

        roleLab = new RoleRepresentation();
        roleLab.setId(roleLabId);
        roleLab.setName(roleLabName);
        roleLabResourceMock = mock(RoleResource.class);
        when(roleLabResourceMock.toRepresentation()).thenReturn(roleLab);
        when(rolesResourceMock.get(roleLabName)).thenReturn(roleLabResourceMock);
        roleScopeResourceMock = mock(RoleScopeResource.class);
        roleMappingResourceMock = mock(RoleMappingResource.class);
        when(roleMappingResourceMock.realmLevel()).thenReturn(roleScopeResourceMock);

        // USER
        userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(firstname);
        userRepresentation.setLastName(lastname);
        userRepresentation.setUsername(username);
        userRepresentation.setId(userid);

        usersResourceMock = mock(UsersResource.class);
        userResourceMock = mock(UserResource.class);
        when(userResourceMock.roles()).thenReturn(roleMappingResourceMock);
        when(userResourceMock.toRepresentation()).thenReturn(userRepresentation);

        when(usersResourceMock.search(username)).thenReturn(List.of(userRepresentation));
        when(usersResourceMock.get(userid)).thenReturn(userResourceMock);

        // GROUPS
        groupsResourceMock = mock(GroupsResource.class);

        // SUB GROUP
        groupRepresentation = new GroupRepresentation();
        groupRepresentation.setName(groupname);
        groupRepresentation.setId(groupid);
        groupRepresentation.setAttributes(Map.of(
            "poc_id", List.of(groupPocId),
            "poc_details", List.of(groupPocDetails)
        ));
        groupResourceMock = mock(GroupResource.class);
        when(groupsResourceMock.group(groupid)).thenReturn(groupResourceMock);
        when(groupResourceMock.toRepresentation()).thenReturn(groupRepresentation);

        // ROOT GROUP
        rootGroupRepresentation = new GroupRepresentation();
        rootGroupRepresentation.setName(rootGroupname);
        rootGroupRepresentation.setId(rootGroupid);
        rootGroupResourceMock = mock(GroupResource.class);
        rootGroupRepresentation.setSubGroups(List.of(groupRepresentation));
        when(groupsResourceMock.group(rootGroupid)).thenReturn(rootGroupResourceMock);
        when(groupsResourceMock.groups(0, Integer.MAX_VALUE)).thenReturn(List.of(rootGroupRepresentation));
        when(rootGroupResourceMock.toRepresentation()).thenReturn(rootGroupRepresentation);

        // REALM
        realmResourceMock = mock(RealmResource.class);
        when(keycloakAdminMock.realm(realm)).thenReturn(realmResourceMock);
        when(realmResourceMock.users()).thenReturn(usersResourceMock);
        when(realmResourceMock.roles()).thenReturn(rolesResourceMock);
        when(realmResourceMock.groups()).thenReturn(groupsResourceMock);
    }

    @Test
    void testUserCreateBothRoles() throws KeycloakService.KeycloakServiceException, URISyntaxException {
        ArgumentCaptor<UserRepresentation> userCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
        when(usersResourceMock.create(userCaptor.capture())).thenReturn(Response.created(new URI("http://example.org")).build());

        ArgumentCaptor<List<RoleRepresentation>> rolesCaptor = ArgumentCaptor.forClass(List.class);
        doNothing().when(roleScopeResourceMock).add(rolesCaptor.capture());

        String createdUserId = keycloakService.createNewUserInGroup(
            firstname,
            lastname,
            username,
            password,
            true,
            true,
            rootGroupPath,
            subGroupPath);

        Assertions.assertEquals(userid, createdUserId);

        Assertions.assertEquals(2, rolesCaptor.getValue().size());
        Assertions.assertEquals(roleCounterName, rolesCaptor.getValue().get(0).getName());
        Assertions.assertEquals(roleCounterId, rolesCaptor.getValue().get(0).getId());
        Assertions.assertEquals(roleLabName, rolesCaptor.getValue().get(1).getName());
        Assertions.assertEquals(roleLabId, rolesCaptor.getValue().get(1).getId());

        Assertions.assertEquals(username, userCaptor.getValue().getUsername());
        Assertions.assertEquals(firstname, userCaptor.getValue().getFirstName());
        Assertions.assertEquals(lastname, userCaptor.getValue().getLastName());
        Assertions.assertEquals(1, userCaptor.getValue().getCredentials().size());
        Assertions.assertEquals(password, userCaptor.getValue().getCredentials().get(0).getValue());
        Assertions.assertEquals("password", userCaptor.getValue().getCredentials().get(0).getType());
        Assertions.assertTrue(userCaptor.getValue().getCredentials().get(0).isTemporary());

        Assertions.assertEquals(2, userCaptor.getValue().getGroups().size());
        Assertions.assertEquals(rootGroupPath, userCaptor.getValue().getGroups().get(0));
        Assertions.assertEquals(subGroupPath, userCaptor.getValue().getGroups().get(1));
    }

    @Test
    void testUserCreateOnlyLabRoles() throws KeycloakService.KeycloakServiceException, URISyntaxException {
        ArgumentCaptor<UserRepresentation> userCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
        when(usersResourceMock.create(userCaptor.capture())).thenReturn(Response.created(new URI("http://example.org")).build());

        ArgumentCaptor<List<RoleRepresentation>> rolesCaptor = ArgumentCaptor.forClass(List.class);
        doNothing().when(roleScopeResourceMock).add(rolesCaptor.capture());

        String createdUserId = keycloakService.createNewUserInGroup(
            firstname,
            lastname,
            username,
            password,
            false,
            true,
            rootGroupPath,
            subGroupPath);

        Assertions.assertEquals(userid, createdUserId);

        Assertions.assertEquals(1, rolesCaptor.getValue().size());
        Assertions.assertEquals(roleLabName, rolesCaptor.getValue().get(0).getName());
        Assertions.assertEquals(roleLabId, rolesCaptor.getValue().get(0).getId());

        Assertions.assertEquals(username, userCaptor.getValue().getUsername());
        Assertions.assertEquals(firstname, userCaptor.getValue().getFirstName());
        Assertions.assertEquals(lastname, userCaptor.getValue().getLastName());
        Assertions.assertEquals(1, userCaptor.getValue().getCredentials().size());
        Assertions.assertEquals(password, userCaptor.getValue().getCredentials().get(0).getValue());
        Assertions.assertEquals("password", userCaptor.getValue().getCredentials().get(0).getType());
        Assertions.assertTrue(userCaptor.getValue().getCredentials().get(0).isTemporary());

        Assertions.assertEquals(2, userCaptor.getValue().getGroups().size());
        Assertions.assertEquals(rootGroupPath, userCaptor.getValue().getGroups().get(0));
        Assertions.assertEquals(subGroupPath, userCaptor.getValue().getGroups().get(1));
    }

    @Test
    void testUserCreateOnlyLabRoleAndOnlyRootGroup() throws KeycloakService.KeycloakServiceException, URISyntaxException {
        ArgumentCaptor<UserRepresentation> userCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
        when(usersResourceMock.create(userCaptor.capture())).thenReturn(Response.created(new URI("http://example.org")).build());

        ArgumentCaptor<List<RoleRepresentation>> rolesCaptor = ArgumentCaptor.forClass(List.class);
        doNothing().when(roleScopeResourceMock).add(rolesCaptor.capture());

        String createdUserId = keycloakService.createNewUserInGroup(
            firstname,
            lastname,
            username,
            password,
            false,
            true,
            rootGroupPath,
            null);

        Assertions.assertEquals(userid, createdUserId);

        Assertions.assertEquals(1, rolesCaptor.getValue().size());
        Assertions.assertEquals(roleLabName, rolesCaptor.getValue().get(0).getName());
        Assertions.assertEquals(roleLabId, rolesCaptor.getValue().get(0).getId());

        Assertions.assertEquals(username, userCaptor.getValue().getUsername());
        Assertions.assertEquals(firstname, userCaptor.getValue().getFirstName());
        Assertions.assertEquals(lastname, userCaptor.getValue().getLastName());
        Assertions.assertEquals(1, userCaptor.getValue().getCredentials().size());
        Assertions.assertEquals(password, userCaptor.getValue().getCredentials().get(0).getValue());
        Assertions.assertEquals("password", userCaptor.getValue().getCredentials().get(0).getType());
        Assertions.assertTrue(userCaptor.getValue().getCredentials().get(0).isTemporary());

        Assertions.assertEquals(1, userCaptor.getValue().getGroups().size());
        Assertions.assertEquals(rootGroupPath, userCaptor.getValue().getGroups().get(0));
    }

    @Test
    void testUserCreateShouldFailWhenUserAlreadyExists() {
        when(usersResourceMock.create(any())).thenReturn(Response.status(HttpStatus.CONFLICT.value()).build());

        KeycloakService.KeycloakServiceException e = Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.createNewUserInGroup(
            firstname,
            lastname,
            username,
            password,
            false,
            true,
            rootGroupPath,
            null));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS, e.getReason());

        verify(roleScopeResourceMock, never()).add(any());
    }

    @Test
    void testUserCreateShouldFailWhenBadRequestReturned() {
        when(usersResourceMock.create(any())).thenReturn(Response.status(HttpStatus.BAD_REQUEST.value()).entity("Error Message").build());

        KeycloakService.KeycloakServiceException e = Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.createNewUserInGroup(
            firstname,
            lastname,
            username,
            password,
            false,
            true,
            rootGroupPath,
            null));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.BAD_REQUEST, e.getReason());

        verify(roleScopeResourceMock, never()).add(any());
    }

    @Test
    void testUserCreateShouldFailWhenAnyStatusElseThanCreatedIsReturned() {
        when(usersResourceMock.create(any())).thenReturn(Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity("Error Message").build());

        KeycloakService.KeycloakServiceException e = Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.createNewUserInGroup(
            firstname,
            lastname,
            username,
            password,
            false,
            true,
            rootGroupPath,
            null));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR, e.getReason());

        verify(roleScopeResourceMock, never()).add(any());
    }

    @Test
    void testUpdateUserNames() throws KeycloakService.KeycloakServiceException {

        ArgumentCaptor<UserRepresentation> userRepresentationArgumentCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
        doNothing().when(userResourceMock).update(userRepresentationArgumentCaptor.capture());

        keycloakService.updateUserNames(userid, "newFirstName", "newLastName");

        Assertions.assertEquals("newFirstName", userRepresentationArgumentCaptor.getValue().getFirstName());
        Assertions.assertEquals("newLastName", userRepresentationArgumentCaptor.getValue().getLastName());
    }

    @Test
    void testUpdateUserNamesNotFound() {

        doThrow(new NotFoundException()).when(userResourceMock).toRepresentation();

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.updateUserNames(userid, "newFirstName", "newLastName"));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND, e.getReason());
        verify(userResourceMock, never()).update(any());
    }

    @Test
    void testUpdateUserRoles_UserHasBothRoles_BothAdded() {
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleCounter, roleLab));

        keycloakService.updateUserRoles(userid, true, true);

        verify(roleScopeResourceMock, never()).remove(any());
        verify(roleScopeResourceMock, never()).add(any());
    }

    @Test
    void testUpdateUserRoles_UserHasLabRole_BothAdded() {
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleLab));

        keycloakService.updateUserRoles(userid, true, true);

        verify(roleScopeResourceMock, never()).remove(any());
        verify(roleScopeResourceMock).add(List.of(roleCounter));
    }

    @Test
    void testUpdateUserRoles_UserHasCounterRole_BothAdded() {
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleCounter));

        keycloakService.updateUserRoles(userid, true, true);

        verify(roleScopeResourceMock, never()).remove(any());
        verify(roleScopeResourceMock).add(List.of(roleLab));
    }

    @Test
    void testUpdateUserRoles_UserHasBothRoles_BothRemoved() {
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleCounter, roleLab));

        keycloakService.updateUserRoles(userid, false, false);

        verify(roleScopeResourceMock).remove(List.of(roleLab, roleCounter));
        verify(roleScopeResourceMock, never()).add(any());
    }

    @Test
    void testUpdateUserRoles_UserHasBothRoles_LabRemoved() {
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleCounter, roleLab));

        keycloakService.updateUserRoles(userid, true, false);

        verify(roleScopeResourceMock).remove(List.of(roleLab));
        verify(roleScopeResourceMock, never()).add(any());
    }

    @Test
    void testUpdateUserRoles_UserHasBothRoles_CounterRemoved() {
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleCounter, roleLab));

        keycloakService.updateUserRoles(userid, false, true);

        verify(roleScopeResourceMock).remove(List.of(roleCounter));
        verify(roleScopeResourceMock, never()).add(any());
    }

    @Test
    void testUpdateUserRoles_UserHasLabRole_CounterAdded() {
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleLab));

        keycloakService.updateUserRoles(userid, true, true);

        verify(roleScopeResourceMock, never()).remove(any());
        verify(roleScopeResourceMock).add(List.of(roleCounter));
    }

    @Test
    void testUpdateUserRoles_UserHasCounterRole_LabAdded() {
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleCounter));

        keycloakService.updateUserRoles(userid, true, true);

        verify(roleScopeResourceMock, never()).remove(any());
        verify(roleScopeResourceMock).add(List.of(roleLab));
    }

    @Test
    void testUpdateUserPassword() {
        ArgumentCaptor<CredentialRepresentation> argumentCaptor = ArgumentCaptor.forClass(CredentialRepresentation.class);
        doNothing().when(userResourceMock).resetPassword(argumentCaptor.capture());

        keycloakService.updateUserPassword(userid, "newPassword");

        Assertions.assertEquals(CredentialRepresentation.PASSWORD, argumentCaptor.getValue().getType());
        Assertions.assertEquals("newPassword", argumentCaptor.getValue().getValue());
        Assertions.assertTrue(argumentCaptor.getValue().isTemporary());
    }

    @Test
    void testGetUserDetails() throws KeycloakService.KeycloakServiceException {
        GroupRepresentation subgroup = new GroupRepresentation();
        subgroup.setId("xxx");
        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleCounter, roleLab));
        when(userResourceMock.groups()).thenReturn(List.of(rootGroupRepresentation, subgroup));

        KeycloakUserResponse userDetails = keycloakService.getUserDetails(userid, rootGroupid);

        Assertions.assertEquals(username, userDetails.getUsername());
        Assertions.assertEquals(firstname, userDetails.getFirstName());
        Assertions.assertEquals(lastname, userDetails.getLastName());
        Assertions.assertEquals(userid, userDetails.getId());
        Assertions.assertEquals(subgroup.getId(), userDetails.getSubGroup());
        Assertions.assertTrue(userDetails.getRoleCounter());
        Assertions.assertTrue(userDetails.getRoleLab());

        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleCounter));
        userDetails = keycloakService.getUserDetails(userid, rootGroupid);
        Assertions.assertTrue(userDetails.getRoleCounter());
        Assertions.assertFalse(userDetails.getRoleLab());

        when(roleScopeResourceMock.listAll()).thenReturn(List.of(roleLab));
        userDetails = keycloakService.getUserDetails(userid, rootGroupid);
        Assertions.assertFalse(userDetails.getRoleCounter());
        Assertions.assertTrue(userDetails.getRoleLab());

        when(roleScopeResourceMock.listAll()).thenReturn(Collections.emptyList());
        userDetails = keycloakService.getUserDetails(userid, rootGroupid);
        Assertions.assertFalse(userDetails.getRoleCounter());
        Assertions.assertFalse(userDetails.getRoleLab());
    }

    @Test
    void testUpdateSubGroupDetails() {
        KeycloakGroupDetails groupDetails = keycloakService.getSubGroupDetails(groupid);

        Assertions.assertEquals(groupid, groupDetails.getId());
        Assertions.assertEquals(groupname, groupDetails.getName());
        Assertions.assertEquals(groupPocId, groupDetails.getPocId());
        Assertions.assertEquals(groupPocDetails, groupDetails.getPocDetails());
    }

    @Test
    void testUpdateSubGroupDetailsNotFound() {
        doThrow(new ClientErrorException(HttpStatus.NOT_FOUND.value())).when(groupResourceMock).toRepresentation();
        Assertions.assertNull(keycloakService.getSubGroupDetails(groupid));
    }

    @Test
    void testGetExtendedUserList() {

        GroupRepresentation subgroup = new GroupRepresentation();
        subgroup.setId("xxx");

        when(groupResourceMock.members(0, Integer.MAX_VALUE)).thenReturn(List.of(userRepresentation));
        when(userResourceMock.groups()).thenReturn(List.of(groupRepresentation, subgroup));

        List<KeycloakUserResponse> response = keycloakService.getExtendedUserListForRootGroup(groupid);

        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(userid, response.get(0).getId());
        Assertions.assertEquals(firstname, response.get(0).getFirstName());
        Assertions.assertEquals(lastname, response.get(0).getLastName());
        Assertions.assertEquals(username, response.get(0).getUsername());
        Assertions.assertEquals(subgroup.getId(), response.get(0).getSubGroup());
        Assertions.assertNull(response.get(0).getRoleCounter());
        Assertions.assertNull(response.get(0).getRoleLab());
    }

    @Test
    void testGetExtendedUserListNoSubgroup() {

        when(groupResourceMock.members(0, Integer.MAX_VALUE)).thenReturn(List.of(userRepresentation));
        when(userResourceMock.groups()).thenReturn(List.of(groupRepresentation));

        List<KeycloakUserResponse> response = keycloakService.getExtendedUserListForRootGroup(groupid);

        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(userid, response.get(0).getId());
        Assertions.assertEquals(firstname, response.get(0).getFirstName());
        Assertions.assertEquals(lastname, response.get(0).getLastName());
        Assertions.assertEquals(username, response.get(0).getUsername());
        Assertions.assertNull(response.get(0).getSubGroup());
        Assertions.assertNull(response.get(0).getRoleCounter());
        Assertions.assertNull(response.get(0).getRoleLab());
    }

    @Test
    void testGetGroupMembers() {

        when(groupResourceMock.members(0, Integer.MAX_VALUE)).thenReturn(List.of(userRepresentation));

        List<UserRepresentation> members = keycloakService.getGroupMembers(groupid);

        verify(groupResourceMock).members(0, Integer.MAX_VALUE);
        Assertions.assertEquals(1, members.size());
        Assertions.assertEquals(userRepresentation, members.get(0));
    }

    @Test
    void testDeleteUser() throws KeycloakService.KeycloakServiceException {
        keycloakService.deleteUser(userid);
        verify(usersResourceMock).delete(userid);

        doThrow(new NotFoundException()).when(usersResourceMock).delete(userid);
        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.deleteUser(userid));
        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND, e.getReason());

        doThrow(new WebApplicationException(Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity("").build())).when(usersResourceMock).delete(userid);
        e = Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.deleteUser(userid));
        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR, e.getReason());
    }

    @Test
    void testDeleteGroup() throws KeycloakService.KeycloakServiceException {
        keycloakService.deleteGroup(groupid);
        verify(groupResourceMock).remove();

        doThrow(new NotFoundException()).when(groupResourceMock).remove();
        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.deleteGroup(groupid));
        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND, e.getReason());

        doThrow(new WebApplicationException(Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity("").build())).when(groupResourceMock).remove();
        e = Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.deleteGroup(groupid));
        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR, e.getReason());
    }

    @Test
    void testMoveGroup() throws KeycloakService.KeycloakServiceException {
        GroupRepresentation childGroup = new GroupRepresentation();
        childGroup.setId("child-group-id");

        GroupResource childGroupResourceMock = mock(GroupResource.class);
        when(childGroupResourceMock.toRepresentation()).thenReturn(childGroup);
        when(groupsResourceMock.group(childGroup.getId())).thenReturn(childGroupResourceMock);

        ArgumentCaptor<GroupRepresentation> captor = ArgumentCaptor.forClass(GroupRepresentation.class);
        when(groupResourceMock.subGroup(captor.capture())).thenReturn(Response.status(HttpStatus.OK.value()).build());

        keycloakService.moveGroup(childGroup.getId(), groupid);

        verify(groupResourceMock).subGroup(any());
        Assertions.assertEquals(childGroup, captor.getValue());
    }

    @Test
    void testMoveGroup_FailedConflict() {
        GroupRepresentation childGroup = new GroupRepresentation();
        childGroup.setId("child-group-id");

        GroupResource childGroupResourceMock = mock(GroupResource.class);
        when(childGroupResourceMock.toRepresentation()).thenReturn(childGroup);
        when(groupsResourceMock.group(childGroup.getId())).thenReturn(childGroupResourceMock);

        when(groupResourceMock.subGroup(any())).thenReturn(Response.status(HttpStatus.CONFLICT.value()).build());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.moveGroup(childGroup.getId(), groupid));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS, e.getReason());
    }

    @Test
    void testMoveGroup_FailedNotFound() {
        GroupRepresentation childGroup = new GroupRepresentation();
        childGroup.setId("child-group-id");

        GroupResource childGroupResourceMock = mock(GroupResource.class);
        when(childGroupResourceMock.toRepresentation()).thenReturn(childGroup);
        when(groupsResourceMock.group(childGroup.getId())).thenReturn(childGroupResourceMock);

        doThrow(new NotFoundException()).when(groupResourceMock).subGroup(any());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.moveGroup(childGroup.getId(), groupid));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND, e.getReason());
    }

    @Test
    void testMoveGroup_OtherError() {
        GroupRepresentation childGroup = new GroupRepresentation();
        childGroup.setId("child-group-id");

        GroupResource childGroupResourceMock = mock(GroupResource.class);
        when(childGroupResourceMock.toRepresentation()).thenReturn(childGroup);
        when(groupsResourceMock.group(childGroup.getId())).thenReturn(childGroupResourceMock);

        doThrow(new WebApplicationException(Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity("").build())).when(groupResourceMock).subGroup(any());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.moveGroup(childGroup.getId(), groupid));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR, e.getReason());
    }

    @Test
    void testMoveUser() throws KeycloakService.KeycloakServiceException {
        when(userResourceMock.groups()).thenReturn(List.of(rootGroupRepresentation, groupRepresentation));

        keycloakService.moveUser(userid, rootGroupid, "newParentId");

        verify(userResourceMock).leaveGroup(groupid);
        verify(userResourceMock, never()).leaveGroup(rootGroupid);

        verify(userResourceMock).joinGroup("newParentId");
    }

    @Test
    void testMoveUser_FailedNotFound() {
        when(userResourceMock.groups()).thenReturn(List.of(rootGroupRepresentation, groupRepresentation));
        doThrow(new NotFoundException()).when(userResourceMock).joinGroup(any());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.moveUser(userid, rootGroupid, "newParentId"));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.NOT_FOUND, e.getReason());
    }

    @Test
    void testMoveUser_FailedOtherError() {
        when(userResourceMock.groups()).thenReturn(List.of(rootGroupRepresentation, groupRepresentation));
        doThrow(new WebApplicationException(Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity("").build())).when(userResourceMock).joinGroup(any());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.moveUser(userid, rootGroupid, "newParentId"));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR, e.getReason());
    }

    @Test
    void testGetRootGroupsOfUser() {
        when(userResourceMock.groups()).thenReturn(List.of(rootGroupRepresentation, groupRepresentation));
        List<GroupRepresentation> rootGroups = keycloakService.getRootGroupsOfUser(userid);

        Assertions.assertEquals(1, rootGroups.size());
        Assertions.assertEquals(rootGroupRepresentation, rootGroups.get(0));
    }

    @Test
    void testUpdateGroup() throws KeycloakService.KeycloakServiceException {

        ArgumentCaptor<GroupRepresentation> captor = ArgumentCaptor.forClass(GroupRepresentation.class);
        doNothing().when(groupResourceMock).update(captor.capture());

        keycloakService.updateGroup(groupid, "newName", "newPocDetails", "newPocId");

        Assertions.assertEquals("newName", captor.getValue().getName());
        Assertions.assertEquals(2, captor.getValue().getAttributes().size());
        Assertions.assertEquals(1, captor.getValue().getAttributes().get("poc_id").size());
        Assertions.assertEquals("newPocId", captor.getValue().getAttributes().get("poc_id").get(0));
        Assertions.assertEquals(1, captor.getValue().getAttributes().get("poc_details").size());
        Assertions.assertEquals("newPocDetails", captor.getValue().getAttributes().get("poc_details").get(0));

    }

    @Test
    void testUpdateGroup_FailedBadRequest() {
        doThrow(new BadRequestException(Response.status(HttpStatus.BAD_REQUEST.value()).entity("").build())).when(groupResourceMock).update(any());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.updateGroup(groupid, "newName", "newPocDetails", "newPocId"));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.BAD_REQUEST, e.getReason());
    }

    @Test
    void testUpdateGroup_FailedOtherError() {
        doThrow(new WebApplicationException(Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity("").build())).when(groupResourceMock).update(any());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.updateGroup(groupid, "newName", "newPocDetails", "newPocId"));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR, e.getReason());
    }

    @Test
    void testCreateGroup() throws KeycloakService.KeycloakServiceException {

        ArgumentCaptor<GroupRepresentation> captor = ArgumentCaptor.forClass(GroupRepresentation.class);
        when(groupResourceMock.subGroup(captor.capture())).thenReturn(Response.status(HttpStatus.OK.value()).build());

        keycloakService.createGroup("newGroupName", "newPocDetails", "newPocId", groupid);

        verify(groupResourceMock).subGroup(any());
        Assertions.assertEquals("newGroupName", captor.getValue().getName());
        Assertions.assertEquals(2, captor.getValue().getAttributes().size());
        Assertions.assertEquals(1, captor.getValue().getAttributes().get("poc_id").size());
        Assertions.assertEquals("newPocId", captor.getValue().getAttributes().get("poc_id").get(0));
        Assertions.assertEquals(1, captor.getValue().getAttributes().get("poc_details").size());
        Assertions.assertEquals("newPocDetails", captor.getValue().getAttributes().get("poc_details").get(0));
    }

    @Test
    void testCreateGroup_FailedConflict() {
        ArgumentCaptor<GroupRepresentation> captor = ArgumentCaptor.forClass(GroupRepresentation.class);
        when(groupResourceMock.subGroup(captor.capture())).thenReturn(Response.status(HttpStatus.CONFLICT.value()).build());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.createGroup("newGroupName", "newPocDetails", "newPocId", groupid));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS, e.getReason());
    }

    @Test
    void testCreateGroup_FailedBadRequest() {
        doThrow(new BadRequestException(Response.status(HttpStatus.BAD_REQUEST.value()).entity("").build())).when(groupResourceMock).subGroup(any());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.createGroup("newGroupName", "newPocDetails", "newPocId", groupid));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.BAD_REQUEST, e.getReason());
    }

    @Test
    void testCreateGroup_FailedOtherError() {
        doThrow(new WebApplicationException(Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity("").build())).when(groupResourceMock).subGroup(any());

        KeycloakService.KeycloakServiceException e =
            Assertions.assertThrows(KeycloakService.KeycloakServiceException.class, () -> keycloakService.createGroup("newGroupName", "newPocDetails", "newPocId", groupid));

        Assertions.assertEquals(KeycloakService.KeycloakServiceException.Reason.SERVER_ERROR, e.getReason());
    }

}

