package app.coronawarn.quicktest.client;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "rkiToolClient",
        url = "${quicktest.health-department-download-url}"
)
public interface RkiToolClient {

    @GetMapping(value = "")
    Response downloadFile();

}
