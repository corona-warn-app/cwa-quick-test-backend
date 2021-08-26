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

import app.coronawarn.quicktest.model.keycloak.KeycloakCreateUserRequest;
import app.coronawarn.quicktest.model.keycloak.KeycloakUpdateUserRequest;
import app.coronawarn.quicktest.model.keycloak.KeycloakUserResponse;
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
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(value = "/api/usermanagement/users")
@RequiredArgsConstructor
@Validated
public class UserManagementController {

    private final UserManagementControllerUtils utils;

    private final KeycloakService keycloakService;

    /**
     * Endpoint to get all users in root group of logged in user.
     */
    @Operation(
        tags = "User Management",
        description = "Get all users."
    )
    @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "List with all users in your root group.",
          content = @Content(array = @ArraySchema(schema = @Schema(implementation = KeycloakUserResponse.class))))
    })
    @GetMapping("")
    @Secured(ROLE_ADMIN)
    public ResponseEntity<List<KeycloakUserResponse>> getUsers(KeycloakAuthenticationToken token) {
        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();

        List<KeycloakUserResponse> extendedUserListForRootGroup =
          keycloakService.getExtendedUserListForRootGroup(userRootGroup.getId());
        return ResponseEntity.ok(extendedUserListForRootGroup);
    }

    /**
     * Endpoint to get details of a specific user.
     */
    @Operation(
        tags = "User Management",
        description = "Get user details."
    )
    @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Object containing details of user.",
          content = @Content(schema = @Schema(implementation = KeycloakUserResponse.class)))
    })
    @GetMapping("/{id}")
    @Secured(ROLE_ADMIN)
    public ResponseEntity<KeycloakUserResponse> getUser(
        KeycloakAuthenticationToken token, @PathVariable("id") String id) {
        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();
        List<String> rootGroupMembers = keycloakService.getGroupMembers(userRootGroup.getId()).stream()
            .map(UserRepresentation::getId)
            .collect(Collectors.toList());

        if (!rootGroupMembers.contains(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not within your subgroup");
        } else {
            try {
                return ResponseEntity.ok(keycloakService.getUserDetails(id, userRootGroup.getId()));
            } catch (KeycloakService.KeycloakServiceException e) {
                if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.NOT_FOUND) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                } else {
                    throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected Error when requesting details of user.");
                }
            }
        }
    }

    /**
     * Endpoint to delete a user.
     */
    @Operation(
        tags = "User Management",
        description = "Delete a user"
    )
    @Parameters({
      @Parameter(in = ParameterIn.PATH, description = "ID of the User")
    })
    @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "User deleted"),
      @ApiResponse(
          responseCode = "403",
          description = "User is not in your root group"),
      @ApiResponse(
          responseCode = "404",
          description = "User not found")
    })
    @DeleteMapping("/{id}")
    @Secured(ROLE_ADMIN)
    public ResponseEntity<Void> deleteUser(KeycloakAuthenticationToken token, @PathVariable("id") String id) {
        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();
        List<String> rootGroupMembers = keycloakService.getGroupMembers(userRootGroup.getId()).stream()
            .map(UserRepresentation::getId)
            .collect(Collectors.toList());

        if (!rootGroupMembers.contains(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not within your subgroup");
        } else {
            try {
                keycloakService.deleteUser(id);
            } catch (KeycloakService.KeycloakServiceException e) {
                if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.NOT_FOUND) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                } else {
                    throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected Error when deleting user.");
                }
            }
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to update user details.
     */
    @Operation(
        tags = "User Management",
        description = "Update a user",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = KeycloakUpdateUserRequest.class)
        ))
    )
    @Parameters({
      @Parameter(in = ParameterIn.PATH, description = "ID of the User")
    })
    @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "User updated"),
      @ApiResponse(
          responseCode = "403",
          description = "User is not in your root group"),
      @ApiResponse(
          responseCode = "404",
          description = "User not found")
    })
    @PatchMapping("/{id}")
    @Secured(ROLE_ADMIN)
    public ResponseEntity<Void> updateUser(
        KeycloakAuthenticationToken token,
        @PathVariable("id") String id,
        @RequestBody KeycloakUpdateUserRequest body) {

        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();
        List<String> rootGroupMembers = keycloakService.getGroupMembers(userRootGroup.getId()).stream()
            .map(UserRepresentation::getId)
            .collect(Collectors.toList());

        if (!rootGroupMembers.contains(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not within your subgroup");
        } else {
            try {
                if (body.getPassword() != null) {
                    keycloakService.updateUserPassword(id, body.getPassword());
                }

                if (body.getRoleCounter() != null && body.getRoleCounter() != null) {
                    keycloakService.updateUserRoles(id, body.getRoleCounter(), body.getRoleLab());
                }

                if (body.getFirstName() != null && body.getLastName() != null) {
                    keycloakService.updateUserNames(id, body.getFirstName(), body.getLastName());
                }
            } catch (KeycloakService.KeycloakServiceException e) {
                if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.NOT_FOUND) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                } else {
                    throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected Error when deleting user.");
                }
            }
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint to create a new user in current root group.
     */
    @Operation(
        tags = "User Management",
        description = "Create a new user within your root group",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = KeycloakCreateUserRequest.class))
        )
    )
    @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "User created and assigned to root group"),
      @ApiResponse(
          responseCode = "400",
          description = "Invalid payload"),
      @ApiResponse(
          responseCode = "409",
          description = "User with username already exists")
    })
    @PostMapping(
        value = "",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Secured(ROLE_ADMIN)
    public ResponseEntity<Void> createNewUser(
        KeycloakAuthenticationToken token, @Valid @RequestBody KeycloakCreateUserRequest body) {

        utils.checkRealm(token);
        GroupRepresentation userRootGroup = utils.checkUserRootGroup();

        String subgroupPath = body.getSubgroup() != null
            ? utils.checkGroupIsInSubgroups(userRootGroup, body.getSubgroup()).getPath()
            : null;

        try {
            keycloakService.createNewUserInGroup(
                body.getFirstName(),
                body.getLastName(),
                body.getUsername(),
                body.getPassword(),
                body.getRoleCounter(),
                body.getRoleLab(),
                userRootGroup.getPath(),
                subgroupPath);
        } catch (KeycloakService.KeycloakServiceException e) {
            if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.ALREADY_EXISTS) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this username already exists");
            } else if (e.getReason() == KeycloakService.KeycloakServiceException.Reason.BAD_REQUEST) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad Request");
            } else {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error when creating new user.");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
