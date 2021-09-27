package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.map.MapCenterList;
import app.coronawarn.quicktest.model.map.MapEntryResponse;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "MapServerClient",
        url = "${quicktest-map-server.url}",
        configuration = QuicktestMapClientConfig.class
)
public interface QuicktestMapClient {
    String AUTH_TOKEN = "Authorization";
    @PostMapping(value = "/api/centers",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<MapEntryResponse> createOrUpdateMapEntry(@RequestHeader(AUTH_TOKEN) String bearerToken,
                                                             @RequestBody @NotNull @Valid MapCenterList mapCenterList);

    @GetMapping(value = "/api/centers/reference/{userReference}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    MapEntryResponse getMapEntry(@RequestHeader(AUTH_TOKEN) String bearerToken,
                                                 @PathVariable String userReference);
}
