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

import app.coronawarn.quicktest.client.QuicktestMapClient;
import app.coronawarn.quicktest.config.KeycloakMapProperties;
import app.coronawarn.quicktest.model.keycloak.KeycloakGroupDetails;
import app.coronawarn.quicktest.model.map.MapCenterList;
import app.coronawarn.quicktest.model.map.MapEntryResponse;
import app.coronawarn.quicktest.model.map.MapEntrySingleResponse;
import app.coronawarn.quicktest.model.map.MapEntryUploadData;
import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.keycloak.admin.client.Keycloak;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class MapEntryService {
    private static String[] OFFERED_TESTS = {"Antigen"};
    private static String APPOINTMENT_REQUIRED = "Required";
    private static String APPOINTMENT_NOT_REQUIRED = "NotRequired";

    private final QuicktestMapClient quicktestMapClient;

    private final Keycloak mapKeycloak;

    private final KeycloakMapProperties config;

    /**
     * create map entry .
     *
     * @param details the group id
     */
    public void createOrUpdateMapEntry(KeycloakGroupDetails details) {
        MapCenterList mapCenterList = new MapCenterList();
        ArrayList<MapEntryUploadData> centers = new ArrayList<>();
        centers.add(buildUploadData(details));
        mapCenterList.setCenters(centers);

        List<MapEntryResponse> response;
        try {
            response = quicktestMapClient.createOrUpdateMapEntry(getBearerToken(), mapCenterList);
        } catch (FeignException e) {
            log.error("Failed to connect to Map Portal Service with Message {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        if (response.isEmpty()) {
            log.error("Failed to add Map Entry response: " + details.getId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Response from MapService is Empty");
        }
    }

    /**
     * queries Service if MapEntry exists .
     *
     * @param reference the group id
     * @return ResponseEntity MapEntryResponse from the MapEntry Service
     */
    public MapEntrySingleResponse getMapEntry(String reference) {
        try {
            MapEntrySingleResponse response = quicktestMapClient.getMapEntry(getBearerToken(), reference);
            if (response != null) {
                return response;
            }
        } catch (FeignException e) {
            log.debug("Failed to connect to MapService with Code {}", e.getMessage());
        }
        return null;
    }

    /**
     * converts Boolean to String for the MapEntryService .
     *
     * @param appointmentRequired the Boolean
     * @return The String value for the Boolean
     */
    public String convertAppointmentToString(Boolean appointmentRequired) {
        if ((appointmentRequired != null) && (appointmentRequired)) {
            return APPOINTMENT_REQUIRED;
        } else {
            return APPOINTMENT_NOT_REQUIRED;
        }
    }

    /**
     * converts Boolean to String for the MapEntryService .
     *
     * @param appointmentRequired the String
     * @return The Boolean value for the supplied String
     */
    public Boolean convertAppointmentToBoolean(String appointmentRequired) {
        return (appointmentRequired != null) && (appointmentRequired.equals(APPOINTMENT_REQUIRED));
    }

    /**
     * queries Service if MapEntry exists .
     * Deletes a MapEntry at MapEntryService if an entry exists for given reference.
     *
     * @param reference the reference of the map entry (QT Group ID)
     */
    public void deleteIfExists(String reference) {
        MapEntrySingleResponse mapEntry = getMapEntry(reference);

        if (mapEntry != null) {
            log.info("Deleting Map Entry for Reference = {}, UUID = {}", reference, mapEntry.getUuid());
            quicktestMapClient.deleteMapEntry(getBearerToken(), mapEntry.getUuid());
        }
    }

    private MapEntryUploadData buildUploadData(KeycloakGroupDetails details) {
        MapEntryUploadData mapEntryUploadData = new MapEntryUploadData();
        mapEntryUploadData.setAddress(details.getPocDetails());
        mapEntryUploadData.setUserReference(details.getId());
        mapEntryUploadData.setTestKinds(OFFERED_TESTS);
        mapEntryUploadData.setDcc(true);
        mapEntryUploadData.setName(details.getName());
        mapEntryUploadData.setEmail(details.getEmail());
        mapEntryUploadData.setAppointment(convertAppointmentToString(details.getAppointmentRequired()));
        mapEntryUploadData.setWebsite(details.getWebsite());
        if (CollectionUtils.isNotEmpty(details.getOpeningHours())) {
            mapEntryUploadData.setOpeningHours(details.getOpeningHours().toArray(String[]::new));
        }

        return mapEntryUploadData;
    }

    private String getBearerToken() {
        return "Bearer " + mapKeycloak.tokenManager().grantToken().getToken();
    }
}
