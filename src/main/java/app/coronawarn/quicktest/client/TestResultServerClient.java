package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.HashedGuid;
import app.coronawarn.quicktest.model.TestResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This class represents the Labor Server service feign client.
 */
@FeignClient(
    name = "testResultServerClient",
    url = "${cwa-testresult-server.url}",
    configuration = TestResultServerClientConfig.class)
public interface TestResultServerClient {

    /**
     * This method gets a testResult from the LabServer.
     *
     * @param guid for TestResult
     * @return TestResult from server
     */
    @PostMapping(value = "/api/v1/app/result",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    TestResult result(HashedGuid guid);
}
