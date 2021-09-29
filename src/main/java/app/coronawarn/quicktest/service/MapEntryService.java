package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.QuicktestMapClient;
import app.coronawarn.quicktest.config.KeycloakMapProperties;
import app.coronawarn.quicktest.model.map.MapCenterList;
import app.coronawarn.quicktest.model.map.MapEntryResponse;
import app.coronawarn.quicktest.model.map.MapEntryUploadData;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class MapEntryService {
    private static String[] OFFERED_TESTS = {"antigen"};

    private final QuicktestMapClient quicktestMapClient;

    private final Keycloak mapKeycloak;

    private final KeycloakMapProperties config;

    /**
     * create map entry .
     *
     * @param reference the group id
     * @param address   the address string
     */
    public void createMapEntry(String reference, String address, String name) {
        MapCenterList mapCenterList = new MapCenterList();
        ArrayList<MapEntryUploadData> centers = new ArrayList<>();
        centers.add(buildUploadData(address, reference, name));
        mapCenterList.setCenters(centers);
        ResponseEntity<List<MapEntryResponse>> response =
            quicktestMapClient.createMapEntry(getBearerToken(), mapCenterList);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to add Map Entry response: " + response.getStatusCode());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * updates map entry .
     *
     * @param reference the group id
     * @param address   the address string
     */
    public void updateMapEntry(String reference, String address, String name) {
        MapEntryUploadData mapEntryUploadData = buildUploadData(address, reference, name);
        ResponseEntity<List<MapEntryResponse>> response =
            quicktestMapClient.updateMapEntry(getBearerToken(), mapEntryUploadData);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to update Map Entry response: " + response.getStatusCode());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a MapEntry at MapEntryService if an entry exists for given reference.
     *
     * @param reference the reference of the map entry (QT Group ID)
     */
    public void deleteIfExists(String reference) {
        String uuid = getMapEntryUuid(reference);

        if (uuid != null) {
            log.info("Deleting Map Entry for Reference = {}, UUID = {}", reference, uuid);
            quicktestMapClient.deleteMapEntry(getBearerToken(), uuid);
        }
    }

    /**
     * Queries Service if MapEntry exists.
     *
     * @param reference the group id
     * @return UUID if MapEntry exists.
     */
    public String getMapEntryUuid(String reference) {
        ResponseEntity<MapEntryResponse> response;

        try {
            response = quicktestMapClient.getMapEntry(getBearerToken(), reference);
        } catch (ResponseStatusException e) {
            if (e.getStatus() != HttpStatus.NOT_FOUND) {
                log.error("Failed to check existence of Map Entry");
            }
            return null;
        }

        if (response.getBody() != null) {
            return response.getBody().getUuid();
        } else {
            log.error("Response has no body.");
            return null;
        }
    }

    private MapEntryUploadData buildUploadData(String address, String reference, String name) {
        MapEntryUploadData mapEntryUploadData = new MapEntryUploadData();
        mapEntryUploadData.setAddress(address);
        mapEntryUploadData.setUserReference(reference);
        mapEntryUploadData.setTestKinds(OFFERED_TESTS);
        mapEntryUploadData.setDcc(true);
        mapEntryUploadData.setName(name);
        return mapEntryUploadData;
    }

    private String getBearerToken() {
        return "Bearer " + mapKeycloak.tokenManager().grantToken().getToken();
    }
}
