package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.QuicktestMapClient;
import app.coronawarn.quicktest.config.KeycloakMapProperties;
import app.coronawarn.quicktest.model.map.MapCenterList;
import app.coronawarn.quicktest.model.map.MapEntryResponse;
import app.coronawarn.quicktest.model.map.MapEntryUploadData;
import feign.FeignException;
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
     * @param address the address string
     */
    public void createOrUpdateMapEntry(String reference, String address,String name) {
        MapCenterList mapCenterList = new MapCenterList();
        ArrayList<MapEntryUploadData> centers = new ArrayList<>();
        centers.add(buildUploadData(address,reference,name));
        mapCenterList.setCenters(centers);
        ResponseEntity<List<MapEntryResponse>>  response =
                quicktestMapClient.createOrUpdateMapEntry(getBearerToken(), mapCenterList);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to add Map Entry response: " + response.getStatusCode());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * queries Service if MapEntry exists .
     *
     * @param reference the group id
     * @return True if MapEntry exists.
     */
    public Boolean doesMapEntryExists(String reference) {
        try {
            ResponseEntity<MapEntryResponse> response = quicktestMapClient.getMapEntry(getBearerToken(), reference);
            if (response.getStatusCode() == HttpStatus.OK) {
                return true;
            }
            return false;
        } catch (FeignException e) {
            log.debug("Failed to connect to MapService with Code {}", e.status());
            return false;
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
