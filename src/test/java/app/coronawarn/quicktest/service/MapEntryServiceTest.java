package app.coronawarn.quicktest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import app.coronawarn.quicktest.client.QuicktestMapClient;
import app.coronawarn.quicktest.model.map.MapEntryResponse;
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
import org.springframework.util.Assert;

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

    ResponseEntity<List< MapEntryResponse >> response;
    MapEntryResponse mapEntryResponse = new MapEntryResponse();
    ResponseEntity<MapEntryResponse> get;
    @BeforeEach
    void setupMocks() {
        mapEntryResponse.setAddress("address");
        mapEntryResponse.setUserReference("ref");
        mapEntryResponse.setName("name");
        List<MapEntryResponse> list = new ArrayList<>();
        list.add(mapEntryResponse);
        response = ResponseEntity.ok(list);
        get = ResponseEntity.ok(mapEntryResponse);
        TokenManager tokenManager = mock(TokenManager.class);
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setToken("");
        when(mapKeycloak.tokenManager()).thenReturn(tokenManager);
        when(mapKeycloak.tokenManager().grantToken()).thenReturn(accessTokenResponse);
    }

    @Test
    void testCreateMapEntry() throws Exception {
        when(mapClient.createOrUpdateMapEntry(anyString(),any())).thenReturn(response);
        mapEntryService.createOrUpdateMapEntry("ref", "address", "name");
    }

    @Test
    void testUpdateMapEntry() throws Exception {
        response.getBody().get(0).setAddress("addressNew");
        when(mapClient.createOrUpdateMapEntry(anyString(),any())).thenReturn(response);
        mapEntryService.createOrUpdateMapEntry("ref", "addressNew", "name");
    }

    @Test
    void testDoesMapEntryExists() throws Exception {
        when(mapClient.getMapEntry(any(),any())).thenReturn(get);
        mapEntryService.doesMapEntryExists("ref");
    }
}
