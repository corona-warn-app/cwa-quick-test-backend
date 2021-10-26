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

import static app.coronawarn.quicktest.config.MtlsSecurityConfig.ROLE_COUNTER;
import static app.coronawarn.quicktest.config.MtlsSecurityConfig.ROLE_LAB;
import static app.coronawarn.quicktest.config.MtlsSecurityConfig.ROLE_PREFIX;

import app.coronawarn.quicktest.config.KeycloakAdminProperties;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupDetails;
import app.coronawarn.quicktest.model.keycloak.KeycloakUserResponse;
import app.coronawarn.quicktest.model.map.MapEntrySingleResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    private final KeycloakAdminProperties config;

    private final QuickTestService quickTestService;

    private final MapEntryService mapEntryService;

    private static final String POC_ID_ATTRIBUTE = "poc_id";
    private static final String POC_DETAILS_ATTRIBUTE = "poc_details";

    /**
     * Creates a new Keycloak User in the Main Realm and sets roles and the root group.
     *
     * @param firstName     First Name of the user.
     * @param lastName      Last Name of the user.
     * @param username      Username of the user.
     * @param password      The initial password for the user. This has to be changed on first login.
     * @param roleCounter   If the Role for Counter should be added.
     * @param roleLab       If the Role for Lab should be added.
     * @param rootGroupPath Path of the ROOT-Group of the user.
     * @return ID of the newly created user.
     * @throws KeycloakServiceException if User Creation has failed.
     */
    public String createNewUserInGroup(
        String firstName,
        String lastName,
        String username,
        String password,
        boolean roleCounter,
        boolean roleLab,
        String rootGroupPath,
        String subGroupPath) throws KeycloakServiceException {

        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(password);
        credentials.setTemporary(true);

        List<String> groupPaths = new ArrayList<>();
        groupPaths.add(rootGroupPath);

        if (subGroupPath != null) {
            groupPaths.add(subGroupPath);
        }

        UserRepresentation newUser = new UserRepresentation();
        newUser.setUsername(username);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setGroups(groupPaths);
        newUser.setEnabled(true);
        newUser.setCredentials(List.of(credentials));

        Response userCreateResponse = realm().users().create(newUser);

        if (userCreateResponse.getStatus() == HttpStatus.CONFLICT.value()) {
            log.error("Failed to create new User: User with this username already exists.");
            throw new KeycloakServiceException(KeycloakServiceException.Reason.ALREADY_EXISTS);
        } else if (HttpStatus.valueOf(userCreateResponse.getStatus()).is4xxClientError()) {
            log.error("Failed to create new user: {} {}",
                userCreateResponse.getStatus(),
                userCreateResponse.readEntity(String.class));

            throw new KeycloakServiceException(KeycloakServiceException.Reason.BAD_REQUEST);
        } else if (userCreateResponse.getStatus() != HttpStatus.CREATED.value()) {
            log.error("Failed to create new user: {} {}",
                userCreateResponse.getStatus(),
                userCreateResponse.readEntity(String.class));

            throw new KeycloakServiceException(KeycloakServiceException.Reason.SERVER_ERROR);
        }

        UserRepresentation createdUser = findUserByUsername(username);
        addRealmRoles(createdUser.getId(), getRoleNames(roleCounter, roleLab));

        return createdUser.getId();
    }

    /**
     * Updates the name of a user.
     *
     * @param userId    ID of the user
     * @param firstName new firstName
     * @param lastName  new LastName
     * @throws KeycloakServiceException if user not exists.
     */
    public void updateUserNames(String userId, String firstName, String lastName) throws KeycloakServiceException {
        UserResource userResource = realm().users().get(userId);
        UserRepresentation user;

        try {
            user = userResource.toRepresentation();
        } catch (NotFoundException e) {
            log.error("User not found");
            throw new KeycloakServiceException(KeycloakServiceException.Reason.NOT_FOUND);
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);

        userResource.update(user);
    }

    /**
     * Updates the roles of a user.
     *
     * @param userId      ID of the user
     * @param roleLab     Whether the user should have ROLE_LAB
     * @param roleCounter Whether the user should have ROLE_COUNTER
     */
    public void updateUserRoles(String userId, boolean roleCounter, boolean roleLab) {
        UserResource userResource = realm().users().get(userId);

        // Delete not anymore needed roles
        Map<String, RoleRepresentation> roles = userResource.roles().realmLevel().listAll().stream()
            .collect(Collectors.toMap(RoleRepresentation::getName, r -> r));
        List<RoleRepresentation> deleteRoles = new ArrayList<>();
        if (!roleLab && roles.containsKey(ROLE_LAB.replace(ROLE_PREFIX, ""))) {
            deleteRoles.add(roles.get(ROLE_LAB.replace(ROLE_PREFIX, "")));
        }

        if (!roleCounter && roles.containsKey(ROLE_COUNTER.replace(ROLE_PREFIX, ""))) {
            deleteRoles.add(roles.get(ROLE_COUNTER.replace(ROLE_PREFIX, "")));
        }

        if (!deleteRoles.isEmpty()) {
            userResource.roles().realmLevel().remove(deleteRoles);
        }


        // Add new roles
        addRealmRoles(userId, getRoleNames(
            (roleCounter && !roles.containsKey(ROLE_COUNTER.replace(ROLE_PREFIX, ""))),
            (roleLab && !roles.containsKey(ROLE_LAB.replace(ROLE_PREFIX, "")))
        ));

    }

    /**
     * Resets the password of a user to a new temporary password.
     *
     * @param userId      ID of the user
     * @param newPassword new temporary password.
     */
    public void updateUserPassword(String userId, String newPassword) {
        CredentialRepresentation newCredentials = new CredentialRepresentation();
        newCredentials.setType(CredentialRepresentation.PASSWORD);
        newCredentials.setTemporary(true);
        newCredentials.setValue(newPassword);

        realm().users().get(userId).resetPassword(newCredentials);
    }

    /**
     * Gets a GroupDetails object by given Group ID. The object contains POC ID and POC Details.
     *
     * @param groupId to search for.
     * @return {@link KeycloakGroupDetails}
     */
    public KeycloakGroupDetails getSubGroupDetails(String groupId) {
        GroupRepresentation group;

        try {
            group = realm().groups().group(groupId).toRepresentation();
        } catch (ClientErrorException e) {
            log.error("Could not find group");
            return null;
        }

        KeycloakGroupDetails groupDetails = new KeycloakGroupDetails();

        groupDetails.setId(groupId);
        groupDetails.setName(group.getName());
        groupDetails.setPocDetails(getFromAttributes(group.getAttributes(), POC_DETAILS_ATTRIBUTE));
        groupDetails.setPocId(getFromAttributes(group.getAttributes(), POC_ID_ATTRIBUTE));
        MapEntrySingleResponse mapEntry = mapEntryService.getMapEntry(groupId);
        if (mapEntry != null) {
            log.info(mapEntry.toString());
            groupDetails.setSearchPortalConsent(true);
            groupDetails.setEmail(mapEntry.getEmail());
            groupDetails.setWebsite(mapEntry.getWebsite());
            groupDetails.setAppointmentRequired(mapEntryService.convertAppointmentToBoolean(
                    mapEntry.getAppointment()));
            if (mapEntry.getOpeningHours() != null) {
                groupDetails.setOpeningHours(
                        mapEntry.getOpeningHours().length > 0 ? mapEntry.getOpeningHours()[0] : null);
            }
        } else {
            groupDetails.setSearchPortalConsent(false);
        }

        return groupDetails;
    }

    /**
     * Gets a KeycloakUserResponse object by given User ID. The object contains information of assigned roles.
     *
     * @param userId      to search for.
     * @param rootGroupId ID of the root group of the user
     * @return {@link KeycloakUserResponse}
     */
    public KeycloakUserResponse getUserDetails(String userId, String rootGroupId) throws KeycloakServiceException {
        UserRepresentation user;
        List<String> realmRoles;
        try {
            user = realm().users().get(userId).toRepresentation();
        } catch (ClientErrorException e) {
            log.error("Could not find user");
            throw new KeycloakServiceException(KeycloakServiceException.Reason.NOT_FOUND);
        }

        try {
            realmRoles = realm().users().get(userId).roles().realmLevel().listAll()
                .stream().map(RoleRepresentation::getName)
                .collect(Collectors.toList());
        } catch (ClientErrorException e) {
            log.error("Could not get user roles");
            throw new KeycloakServiceException(KeycloakServiceException.Reason.SERVER_ERROR);
        }

        KeycloakUserResponse userResponse = new KeycloakUserResponse();
        userResponse.setUsername(user.getUsername());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setId(user.getId());
        userResponse.setSubGroup(getSubgroupId(userId, rootGroupId));
        userResponse.setRoleCounter(realmRoles.contains(ROLE_COUNTER.replace(ROLE_PREFIX, "")));
        userResponse.setRoleLab(realmRoles.contains(ROLE_LAB.replace(ROLE_PREFIX, "")));

        return userResponse;
    }

    /**
     * Gets a list of all user inside a given Root Group.
     * The returned User Object contains also information about assigned roles and assigned subgroups.
     *
     * @param groupId the ID of the root group.
     * @return List of KeycloakUserResponse Objects.
     */
    public List<KeycloakUserResponse> getExtendedUserListForRootGroup(String groupId) {
        return getGroupMembers(groupId).stream()
            .map(member -> {
                KeycloakUserResponse userResponse = new KeycloakUserResponse();
                userResponse.setId(member.getId());
                userResponse.setFirstName(member.getFirstName());
                userResponse.setLastName(member.getLastName());
                userResponse.setUsername(member.getUsername());
                userResponse.setRoleLab(null);
                userResponse.setRoleCounter(null);
                userResponse.setSubGroup(getSubgroupId(member.getId(), groupId));
                return userResponse;
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets a list of group members.
     *
     * @param groupId id of the group
     * @return List of {@link UserRepresentation}
     */
    public List<UserRepresentation> getGroupMembers(String groupId) {
        return realm().groups().group(groupId).members(0, Integer.MAX_VALUE);
    }

    /**
     * Deletes a user.
     *
     * @param userId ID of the user to be deleted
     * @throws KeycloakServiceException if something went wrong
     */
    public void deleteUser(String userId) throws KeycloakServiceException {
        try {
            log.info("Deleting user with id {}", userId);
            realm().users().delete(userId);
            log.info("Deleted user with id {}", userId);
        } catch (NotFoundException e) {
            log.error("Failed to delete user: Not found");
            throw new KeycloakServiceException(KeycloakServiceException.Reason.NOT_FOUND);
        } catch (WebApplicationException e) {
            log.error("Failed to delete user: {}", e.getResponse().readEntity(String.class));
            throw new KeycloakServiceException(KeycloakServiceException.Reason.SERVER_ERROR);
        }
    }

    /**
     * Deletes a group from Keycloak.
     *
     * @param groupId ID opf the group
     * @throws KeycloakServiceException if something went wrong.
     */
    public void deleteGroup(String rootGroupName, String groupId) throws KeycloakServiceException {

        GroupRepresentation group = realm().groups().group(groupId).toRepresentation();

        List<String> subGroupPocIds = getSubGroupIds(group).stream()
            .map(this::getSubGroupDetails)
            .filter(Objects::nonNull)
            .map(KeycloakGroupDetails::getPocId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (quickTestService.pendingTestsForTenantAndPocsExists(rootGroupName, subGroupPocIds)) {
            log.info("User tried to delete a group with pending quick tests");
            throw new KeycloakServiceException(KeycloakServiceException.Reason.NOT_ALLOWED);
        }

        try {
            realm().groups().group(groupId).remove();
            mapEntryService.deleteIfExists(groupId);
            for (String subGroupId:subGroupPocIds) {
                mapEntryService.deleteIfExists(subGroupId);
            }
            log.info("Deleted group with id {}", groupId);
        } catch (NotFoundException e) {
            log.error("Failed to delete group: NOT FOUND");
            throw new KeycloakServiceException(KeycloakServiceException.Reason.NOT_FOUND);
        } catch (WebApplicationException e) {
            log.error("Failed to delete group: SERVER ERROR {}", e.getResponse().readEntity(String.class));
            throw new KeycloakServiceException(KeycloakServiceException.Reason.SERVER_ERROR);
        }
    }

    private List<String> getSubGroupIds(GroupRepresentation group) {
        List<String> groupIds = new ArrayList<>();
        getSubGroupIdsRecursion(groupIds, group);

        return groupIds;

    }

    private void getSubGroupIdsRecursion(List<String> ids, GroupRepresentation group) {
        ids.add(group.getId());

        group.getSubGroups()
            .forEach(g -> getSubGroupIdsRecursion(ids, g));
    }

    /**
     * Moves a group into another group.
     *
     * @param groupId   ID of the group to be moved
     * @param newParent ID of the new parent of the group.
     * @throws KeycloakServiceException if something went wrong.
     */
    public void moveGroup(String groupId, String newParent) throws KeycloakServiceException {
        try {
            Response response = realm().groups().group(newParent).subGroup(
                realm().groups().group(groupId).toRepresentation()
            );

            if (response.getStatus() == HttpStatus.CONFLICT.value()) {
                log.error("Failed to move group: CONFLICT");
                throw new KeycloakServiceException(KeycloakServiceException.Reason.ALREADY_EXISTS);
            }

            log.info("Moved group {} into group {}", groupId, newParent);
        } catch (NotFoundException e) {
            log.error("Failed to move group: NOT FOUND");
            throw new KeycloakServiceException(KeycloakServiceException.Reason.NOT_FOUND);
        } catch (WebApplicationException e) {
            log.error("Failed to move group: SERVER ERROR {}", e.getResponse().readEntity(String.class));
            throw new KeycloakServiceException(KeycloakServiceException.Reason.SERVER_ERROR);
        }
    }

    /**
     * Moves a user into another group.
     * This method removes the user from all groups except its root group and add him to the new group.
     *
     * @param userId      ID of the user to be moved
     * @param rootGroupId ID of the root group of the user.
     * @param newParent   ID of the new parent of the user.
     * @throws KeycloakServiceException if something went wrong.
     */
    public void moveUser(String userId, String rootGroupId, String newParent) throws KeycloakServiceException {
        try {

            log.info("Moving User {} into group {}", userId, newParent);

            UserResource userResource = realm().users().get(userId);

            // Remove user from all groups except root group
            realm().users().get(userId).groups().stream()
                .filter(group -> !group.getId().equals(rootGroupId))
                .forEach(group -> userResource.leaveGroup(group.getId()));

            realm().users().get(userId).joinGroup(newParent);
            log.info("Moved user {} into group {}", userId, newParent);
        } catch (NotFoundException e) {
            log.error("Failed to move user: NOT FOUND");
            throw new KeycloakServiceException(KeycloakServiceException.Reason.NOT_FOUND);
        } catch (WebApplicationException e) {
            log.error("Failed to move user: SERVER ERROR {}", e.getResponse().readEntity(String.class));
            throw new KeycloakServiceException(KeycloakServiceException.Reason.SERVER_ERROR);
        }
    }

    /**
     * Gets the list of Root-Level-Groups for a given User ID.
     *
     * @param userId ID of the user.
     * @return List of Groups.
     */
    public List<GroupRepresentation> getRootGroupsOfUser(String userId) {
        List<GroupRepresentation> rootGroups = realm().groups().groups(0, Integer.MAX_VALUE);

        List<String> userGroupIds = realm().users().get(userId).groups().stream()
            .map(GroupRepresentation::getId)
            .collect(Collectors.toList());

        return rootGroups.stream()
            .filter(rg -> userGroupIds.contains(rg.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Updates details of a Group.
     *
     * @param details          The KeycloakGroupDetails object
     */
    public void updateGroup(KeycloakGroupDetails details)
            throws KeycloakServiceException {
        log.info("Updating group with id {}", details.getId());
        GroupResource groupResource = realm().groups().group(details.getId());
        GroupRepresentation group;
        try {
            group = groupResource.toRepresentation();
        } catch (NotFoundException e) {
            throw new KeycloakServiceException(KeycloakServiceException.Reason.NOT_FOUND);
        }

        group.setName(details.getName());
        // do not update POC ID
        group.setAttributes(getGroupAttributes(details.getPocDetails(),
                getFromAttributes(group.getAttributes(), POC_ID_ATTRIBUTE)));

        try {
            groupResource.update(group);
            if (details.getSearchPortalConsent()) {
                mapEntryService.createOrUpdateMapEntry(details);
            } else {
                mapEntryService.deleteIfExists(group.getId());
            }
            log.info("updated group");
        } catch (BadRequestException e) {
            log.error("Failed to update group: BAD REQUEST {}", e.getResponse().readEntity(String.class));
            throw new KeycloakServiceException(KeycloakServiceException.Reason.BAD_REQUEST);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == HttpStatus.CONFLICT.value()) {
                log.error("Failed to update group: CONFLICT {}", e.getResponse().readEntity(String.class));
                throw new KeycloakServiceException(KeycloakServiceException.Reason.ALREADY_EXISTS);
            } else {
                log.error("Failed to update group: SERVER ERROR {}", e.getResponse().readEntity(String.class));
                throw new KeycloakServiceException(KeycloakServiceException.Reason.SERVER_ERROR);
            }
        }
    }

    /**
     * Create a new subgroup.
     *
     * @param details          The KeycloakGroupDetails object
     */
    public void createGroup(KeycloakGroupDetails details, String parent)
        throws KeycloakServiceException {
        log.info("Creating new group");
        GroupRepresentation newGroup = new GroupRepresentation();
        newGroup.setName(details.getName());

        try {
            Response response = realm().groups().group(parent).subGroup(newGroup);
            if (response.getStatus() == HttpStatus.CONFLICT.value()) {
                log.error("Failed to create group: CONFLICT");
                throw new KeycloakServiceException(KeycloakServiceException.Reason.ALREADY_EXISTS);
            }
            log.info("created group");
            // setting group properties with Group Details and POC ID
            newGroup = response.readEntity(GroupRepresentation.class);
            newGroup.setAttributes(getGroupAttributes(details.getPocDetails(), newGroup.getId()));
            realm().groups().group(newGroup.getId()).update(newGroup);
            if (details.getSearchPortalConsent()) {
                details.setId(newGroup.getId());
                mapEntryService.createOrUpdateMapEntry(details);
                log.info("created mapEntry for Group");
            }
        } catch (BadRequestException e) {
            log.error("Failed to create group: BAD REQUEST {}", e.getResponse().readEntity(String.class));
            throw new KeycloakServiceException(KeycloakServiceException.Reason.BAD_REQUEST);
        } catch (WebApplicationException e) {
            log.error("Failed to create group: SERVER ERROR {}", e.getResponse().readEntity(String.class));
            throw new KeycloakServiceException(KeycloakServiceException.Reason.SERVER_ERROR);
        }
    }

    /**
     * get group by name.
     *
     * @param name the name of the group
     * @return the representation of the group.
     */
    public Optional<GroupRepresentation> getGroup(String name) {
        String path = name.startsWith("/") ? name : "/" + name;
        log.debug("Getting group: [{}]", path);
        return realm().groups().groups(name, 0, Integer.MAX_VALUE)
            .stream()
            .filter(group -> group.getPath().equals(path))
            .findFirst();
    }

    /**
     * Gets the Subgroup of a user.
     *
     * @param userId      id of the user
     * @param rootGroupId rootGroup ID to filter
     * @return ID of the subgroup of a user
     */
    private String getSubgroupId(String userId, String rootGroupId) {
        return realm().users().get(userId).groups().stream()
            .filter(group -> !group.getId().equals(rootGroupId))
            .findFirst()
            .map(GroupRepresentation::getId)
            .orElse(null);
    }

    private List<String> getRoleNames(boolean roleCounter, boolean roleLab) {
        List<String> roleNames = new ArrayList<>();
        if (roleCounter) {
            roleNames.add(ROLE_COUNTER.replace(ROLE_PREFIX, ""));
        }

        if (roleLab) {
            roleNames.add(ROLE_LAB.replace(ROLE_PREFIX, ""));
        }

        return roleNames;
    }

    private UserRepresentation findUserByUsername(String username) throws KeycloakServiceException {
        List<UserRepresentation> foundUsers = realm().users().search(username);

        if (foundUsers.size() != 1) {
            throw new KeycloakServiceException(KeycloakServiceException.Reason.NOT_FOUND);
        } else {
            return foundUsers.get(0);
        }
    }

    private void addRealmRoles(String userId, List<String> roleNames) {
        List<RoleRepresentation> roles = roleNames.stream()
            .map(roleName -> realm().roles().get(roleName).toRepresentation())
            .collect(Collectors.toList());

        if (!roles.isEmpty()) {
            realm().users().get(userId).roles().realmLevel().add(roles);
        }
    }

    private String getFromAttributes(Map<String, List<String>> attributes, String key) {
        if (attributes == null) {
            return null;
        }

        List<String> attributeValues = attributes.get(key);

        if (attributeValues != null && attributeValues.size() >= 1) {
            return attributeValues.get(0);
        } else {
            return null;
        }
    }

    private Map<String, List<String>> getGroupAttributes(String pocDetails, String pocId) {
        Map<String, List<String>> attributes = new HashMap<>();
        if (pocDetails != null) {
            attributes.put(POC_DETAILS_ATTRIBUTE, List.of(pocDetails));
        }

        if (pocId != null) {
            attributes.put(POC_ID_ATTRIBUTE, List.of(pocId));
        }

        return attributes;
    }

    private RealmResource realm() {
        return keycloak.realm(config.getRealm());
    }

    @Getter
    public static class KeycloakServiceException extends Exception {

        Reason reason;

        public KeycloakServiceException(Reason reason) {
            this.reason = reason;
        }

        public enum Reason {
            ALREADY_EXISTS,
            NOT_FOUND,
            SERVER_ERROR,
            BAD_REQUEST,
            NOT_ALLOWED
        }
    }

}
