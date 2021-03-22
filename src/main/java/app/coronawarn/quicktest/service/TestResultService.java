package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.TestResultServerClient;
import app.coronawarn.quicktest.model.HashedGuid;
import app.coronawarn.quicktest.model.TestResult;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultServerClient testResultServerClient;

    /**
     * This method gives an TestResult for a guid.
     *
     * @param guid hashed GUID
     * @return Testresult for GUID
     */
    public TestResult result(HashedGuid guid) {
        return testResultServerClient.result(guid);
    }
}
