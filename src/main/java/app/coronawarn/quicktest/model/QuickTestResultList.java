package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Schema(
        description = "The quick test result list model."
)
@Getter
@ToString
@EqualsAndHashCode
public class QuickTestResultList {

    @NotNull
    @NotEmpty
    private List<@Valid QuickTestResult> testResults;

    public QuickTestResultList setTestResults(List<QuickTestResult> quickTestResults) {
        this.testResults = quickTestResults;
        return this;
    }

}
