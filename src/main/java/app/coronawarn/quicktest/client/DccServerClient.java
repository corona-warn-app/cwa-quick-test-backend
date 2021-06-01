package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.DccPublicKeyList;
import app.coronawarn.quicktest.model.DccUploadData;
import app.coronawarn.quicktest.model.DccUploadResult;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "dccServerClient",
        url = "${cwa-dcc-server.url}",
        configuration = DccServerClientConfig.class
)
public interface DccServerClient {
    @GetMapping(value = "/version/v1/publicKey/search/{labId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    DccPublicKeyList searchPublicKeys(@PathVariable("labId") String labId);

    @PostMapping(value = "/version/v1/test/{testId}/dcc",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    DccUploadResult uploadDcc(@PathVariable("testId") String testId,
                              @RequestBody @NotNull @Valid DccUploadData dccUploadData);


}
