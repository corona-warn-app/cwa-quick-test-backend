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

package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_ADMIN;

import app.coronawarn.quicktest.model.keycloak.KeycloakGroupDetails;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupId;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupResponse;
import app.coronawarn.quicktest.model.keycloak.KeycloakUserId;
import app.coronawarn.quicktest.service.KeycloakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(value = "/api/usermanagement/groups")
@RequiredArgsConstructor
@Validated
public class GroupManagementController {

    private final UserManagementControllerUtils utils;

    private final KeycloakService keycloakService;

    private final CancellationUtils cancellationUtils;

    /**
     * Endpoint to get groups in Root Group of User.
     */
    @Operation(
        tags = "User Management",
        description = "Get subgroups of currently logged in user"
    )
    @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "List of Groups",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = KeycloakGroupResponse.class))))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_ADMIN)
    public ResponseEntity<List<KeycloakGroupResponse>> getSubGroups(KeycloakAuthenticationToken token) {
        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();
        List<KeycloakGroupResponse> groups = new ArrayList<>();
        utils.convertGroups(groups, userRootGroup.getSubGroups(), true);

        return ResponseEntity.ok(groups);
    }

    /**
     * Endpoint to get a specific group.
     */
    @Operation(
        tags = "User Management",
        description = "Get Details of a group"
    )
    @Parameters({
      @Parameter(in = ParameterIn.PATH, name = "id", description = "ID of the group")
    })
    @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Group Details",
          content = @Content(schema = @Schema(implementation = KeycloakGroupDetails.class))),
      @ApiResponse(
          responseCode = "403",
          description = "Group is not in your subgroups"),
      @ApiResponse(
          responseCode = "404",
          description = "Group Not Found")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_ADMIN)
    public ResponseEntity<KeycloakGroupDetails> getSubGroupDetails(
        KeycloakAuthenticationToken token,
        @PathVariable("id") String id
    ) {
        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();
        utils.checkGroupIsInSubgroups(userRootGroup, id);

        KeycloakGroupDetails groupDetails = keycloakService.getSubGroupDetails(id);

        if (groupDetails == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        } else {
            return ResponseEntity.ok(groupDetails);
        }
    }

    /**
     * Endpoint to create a group.
     */
    @Operation(
        tags = "User Management",
        description = "Create a new group within your root group",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = KeycloakGroupDetails.class)))
    )
    @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Group Created"),
      @ApiResponse(
          responseCode = "409",
          description = "Group with this name already exists")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_ADMIN)
    public ResponseEntity<Void> createSubGroup(
        KeycloakAuthenticationToken token,
        @Valid @RequestBody KeycloakGroupDetails body
    ) {
        if (cancellationUtils.isCancellationStarted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cancellation already started, endpoint is not available anymore.");
        }

        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();

        try {
            keycloakService.createGroup(body, userRootGroup.getId());
        } catch (KeycloakService.KeycloakServiceException e) {
            if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
            } else if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Group with this name already exists");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update group");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Endpoint to update a group.
     */
    @Operation(
        tags = "User Management",
        description = "Update a group within your root group",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = KeycloakGroupDetails.class)))
    )
    @Parameters({
      @Parameter(in = ParameterIn.PATH, name = "id", description = "ID of the group")
    })
    @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "Group Updated"),
      @ApiResponse(
          responseCode = "403",
          description = "Group is not in your root group."),
      @ApiResponse(
          responseCode = "404",
          description = "Group not found.")
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_ADMIN)
    public ResponseEntity<Void> updateSubGroupDetails(
        KeycloakAuthenticationToken token,
        @PathVariable("id") String id,
        @Valid @RequestBody KeycloakGroupDetails body
    ) {
        if (cancellationUtils.isCancellationStarted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cancellation already started, endpoint is not available anymore.");
        }

        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();
        utils.checkGroupIsInSubgroups(userRootGroup, id);
        utils.checkPermissionsBasedOnInput(token, body);

        try {
            keycloakService.updateGroup(body);
        } catch (KeycloakService.KeycloakServiceException e) {
            if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
            } else if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Group with this name already exists");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update group");
            }
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to add a group as children of another group.
     */
    @Operation(
        tags = "User Management",
        description = "Add Group as children of another group",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Containing the ID of the group which should be added to parent group",
            content = @Content(schema = @Schema(implementation = KeycloakGroupId.class)))
    )
    @Parameters({
      @Parameter(in = ParameterIn.PATH, name = "id", description = "ID of the parent group")
    })
    @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "Group relation updated"),
      @ApiResponse(
          responseCode = "403",
          description = "Group is not in your root group."),
      @ApiResponse(
          responseCode = "404",
          description = "Group not found."),
      @ApiResponse(
          responseCode = "409",
          description = "A group with the name of the child group already exists in the parent's subgroups.")
    })
    @PostMapping(value = "/{parentId}/subgroups", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_ADMIN)
    public ResponseEntity<Void> addSubGroup(
        KeycloakAuthenticationToken token,
        @PathVariable("parentId") String parentId,
        @Valid @RequestBody KeycloakGroupId body
    ) {
        if (cancellationUtils.isCancellationStarted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cancellation already started, endpoint is not available anymore.");
        }

        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();

        if (!parentId.equals(userRootGroup.getId())) { // Allow to put group into root group
            utils.checkGroupIsInSubgroups(userRootGroup, parentId);
        }
        utils.checkGroupIsInSubgroups(userRootGroup, body.getGroupId());

        try {
            keycloakService.moveGroup(body.getGroupId(), parentId);
        } catch (KeycloakService.KeycloakServiceException e) {
            if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group(s) not found");
            } else if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not add group because it already exists");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to move group");
            }
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to add a user as children of a group.
     */
    @Operation(
        tags = "User Management",
        description = "Add a user as children of a group. "
            + "The user will be automatically removed from all other subgroups",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Containing the ID of the user which should be added to parent group",
            content = @Content(schema = @Schema(implementation = KeycloakUserId.class)))
    )
    @Parameters({
      @Parameter(in = ParameterIn.PATH, name = "id", description = "ID of the parent group")
    })
    @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "User assigned to group"),
      @ApiResponse(
          responseCode = "403",
          description = "Group or User is not in your root group."),
      @ApiResponse(
          responseCode = "404",
          description = "Group or User not found.")
    })
    @PostMapping(value = "/{parentId}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_ADMIN)
    public ResponseEntity<Void> addUserToGroup(
        KeycloakAuthenticationToken token,
        @PathVariable("parentId") String parentId,
        @Valid @RequestBody KeycloakUserId body
    ) {
        if (cancellationUtils.isCancellationStarted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cancellation already started, endpoint is not available anymore.");
        }

        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();

        if (!parentId.equals(userRootGroup.getId())) { // Allow to put group into root group
            utils.checkGroupIsInSubgroups(userRootGroup, parentId);
        }
        List<String> userIds = keycloakService.getGroupMembers(userRootGroup.getId()).stream()
            .map(UserRepresentation::getId)
            .collect(Collectors.toList());

        if (!userIds.contains(body.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not in your root group");
        }

        try {
            keycloakService.moveUser(body.getUserId(), userRootGroup.getId(), parentId);
        } catch (KeycloakService.KeycloakServiceException e) {
            if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or group not found");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to move group");
            }
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to delete a group.
     */
    @Operation(
        tags = "User Management",
        description = "Delete a group.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Containing the ID of the group which should be deleted",
            content = @Content(schema = @Schema(implementation = KeycloakGroupId.class)))
    )
    @Parameters({
      @Parameter(in = ParameterIn.PATH, name = "id", description = "ID of the group")
    })
    @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "Group deleted."),
      @ApiResponse(
          responseCode = "403",
          description = "Group is not in your root group."),
      @ApiResponse(
          responseCode = "404",
          description = "Group not found.")
    })
    @DeleteMapping("/{id}")
    @Secured(ROLE_ADMIN)
    public ResponseEntity<Void> deleteSubGroupDetails(
        KeycloakAuthenticationToken token,
        @PathVariable("id") String id
    ) {
        if (cancellationUtils.isCancellationStarted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cancellation already started, endpoint is not available anymore.");
        }

        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();
        utils.checkGroupIsInSubgroups(userRootGroup, id);

        try {
            keycloakService.deleteGroup(userRootGroup.getName(), id);
        } catch (KeycloakService.KeycloakServiceException e) {
            if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
            } else if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.NOT_ALLOWED) {
                throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
                    "Group or one of its subgroups has assigned pending quick tests");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete group");
            }
        }

        return ResponseEntity.noContent().build();
    }
}
