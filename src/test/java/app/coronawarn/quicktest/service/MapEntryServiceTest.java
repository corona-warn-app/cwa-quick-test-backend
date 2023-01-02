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

package app.coronawarn.quicktest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.client.QuicktestMapClient;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupDetails;
import app.coronawarn.quicktest.model.map.MapEntryResponse;
import app.coronawarn.quicktest.model.map.MapEntrySingleResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

@Slf4j
@SpringBootTest
public class MapEntryServiceTest {
    @Autowired
    MapEntryService mapEntryService;

    @MockBean
    QuicktestMapClient mapClient;

    @MockBean
    @Qualifier(value = "mapKeycloak")
    Keycloak mapKeycloak;


    MapEntryResponse mapEntryResponse = new MapEntryResponse();
    MapEntrySingleResponse mapEntrySingleResponse = new MapEntrySingleResponse();
    ResponseEntity<MapEntryResponse> get;
    private KeycloakGroupDetails groupDetails;
    List<MapEntryResponse> list = new ArrayList<>();

    @BeforeEach
    void setupMocks() {
        mapEntryResponse.setAddress("address");
        mapEntryResponse.setUserReference("ref");
        mapEntryResponse.setName("name");

        mapEntrySingleResponse.setAddress("address");
        mapEntrySingleResponse.setUserReference("ref");
        mapEntrySingleResponse.setName("name");
        list.add(mapEntryResponse);
        get = ResponseEntity.ok(mapEntryResponse);
        TokenManager tokenManager = mock(TokenManager.class);
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setToken("");
        when(mapKeycloak.tokenManager()).thenReturn(tokenManager);
        when(mapKeycloak.tokenManager().grantToken()).thenReturn(accessTokenResponse);
        groupDetails = new KeycloakGroupDetails();
        groupDetails.setName("newGroupName");
        groupDetails.setPocDetails("newPocDetails");
        groupDetails.setPocId("pocId");
        groupDetails.setSearchPortalConsent(false);
    }

    @Test
    void testCreateMapEntry() throws Exception {
        when(mapClient.createOrUpdateMapEntry(anyString(),any())).thenReturn(list);
        mapEntryService.createOrUpdateMapEntry(groupDetails);
    }

    @Test
    void testUpdateMapEntry() throws Exception {
        list.get(0).setAddress("addressNew");
        when(mapClient.createOrUpdateMapEntry(anyString(),any())).thenReturn(list);
        mapEntryService.createOrUpdateMapEntry(groupDetails);
    }

    @Test
    void testDoesMapEntryExists() throws Exception {
        when(mapClient.getMapEntry(any(),any())).thenReturn(mapEntrySingleResponse);
        mapEntryService.getMapEntry("ref");
    }
}
