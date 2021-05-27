package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.DccUploadData;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "dccServerClient",
        url = "${cwa-dcc-server.url}",
        // TODO configuration for DccServer
        configuration = TestResultServerClientConfig.class
)
public interface DccServerClient {
    @PostMapping(value = "/api/v1/publicKey/search",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Map<String,String> searchPublicKeys(List<String> testIds);

    @PostMapping(value = "/api/v1/test/{testId}/dcc",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    void uploadDCC(@RequestBody @NotNull @Valid DccUploadData dccUploadData);


}
