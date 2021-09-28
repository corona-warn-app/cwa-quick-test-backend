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

        List<MapEntryResponse>  response = null;
        try {
            response = quicktestMapClient.createOrUpdateMapEntry(getBearerToken(), mapCenterList);
        } catch (FeignException e) {
            log.error("Failed to connect to Map Portal Service with Message {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        if (response.isEmpty()) {
            log.error("Failed to add Map Entry response: " + response.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Response from MapService is Empty");
        }
    }

    /**
     * queries Service if MapEntry exists .
     *
     * @param reference the group id
     * @return ResponseEntity MapEntryResponse from the MapEntry Service
     */
    public  MapEntrySingleResponse getMapEntry(String reference) {
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
        if ((appointmentRequired != null) && (appointmentRequired.equals(APPOINTMENT_REQUIRED))) {
            return true;
        } else {
            return false;
        }
    }

    private MapEntryUploadData buildUploadData(KeycloakGroupDetails details) {
        MapEntryUploadData mapEntryUploadData = new MapEntryUploadData();
        mapEntryUploadData.setAddress(details.getPocDetails());
        mapEntryUploadData.setUserReference(details.getId());
        mapEntryUploadData.setTestKinds(OFFERED_TESTS);
        mapEntryUploadData.setDcc(true);
        mapEntryUploadData.setName(details.getName());
        mapEntryUploadData.setAppointment(convertAppointmentToString(details.getAppointmentRequired()));
        mapEntryUploadData.setWebsite(details.getWebsite());
        String[] openingHours = {details.getOpeningHours()};
        mapEntryUploadData.setOpeningHours(openingHours);

        return mapEntryUploadData;
    }

    private String getBearerToken() {
        return "Bearer " + mapKeycloak.tokenManager().grantToken().getToken();
    }
}
