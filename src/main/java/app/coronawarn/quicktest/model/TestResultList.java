package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Model of the test result list.
 */
@Schema(
        description = "The test result list model."
)
@Getter
@ToString
@EqualsAndHashCode
public class TestResultList {

    /**
     * The test result entries.
     */
    @NotNull
    @NotEmpty
    private List<@Valid TestResult> testResults;

    public TestResultList setTestResults(List<TestResult> testResults) {
        this.testResults = testResults;
        return this;
    }
}
