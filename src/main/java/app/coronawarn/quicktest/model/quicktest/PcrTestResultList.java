package app.coronawarn.quicktest.model.quicktest;


import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Schema(
  description = "The pcr test result list model."
)
@Getter
@ToString
@EqualsAndHashCode
public class PcrTestResultList {

    @NotNull
    @NotEmpty
    private List<@Valid PcrTestResult> testResults;

    public PcrTestResultList setTestResults(List<PcrTestResult> pcrTestResults) {
        this.testResults = pcrTestResults;
        return this;
    }

    private String labId;

    public void setLabId(String labId) {
        this.labId = labId;
    }

}
