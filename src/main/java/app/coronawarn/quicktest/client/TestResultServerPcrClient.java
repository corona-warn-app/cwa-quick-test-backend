package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.quicktest.PcrTestResultList;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "testResultServerPcrClient",
        url = "${cwa-testresult-server.url-pcr}",
        configuration = TestResultServerPcrClientConfig.class
)
public interface TestResultServerPcrClient {

    /**
     * Insert or update the pcr test results.
     *
     * @param testResults for TestResults
     */
    @PostMapping(value = "/api/v1/lab/results",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> pcrResults(@RequestBody @NotNull @Valid PcrTestResultList testResults);
}
