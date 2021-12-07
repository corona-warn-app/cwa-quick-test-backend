package app.coronawarn.quicktest.model.quicktest;

import app.coronawarn.quicktest.validation.ValidGuid;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Model of the pcr test result.
 */
@Schema(
  description = "The pcr test result model."
)
@Getter
@ToString
@EqualsAndHashCode
public class PcrTestResult {

    /**
     * Hash (SHA256) of test result id (aka QR-Code, GUID) encoded as hex string.
     */
    @NotBlank
    @ValidGuid
    private String id;

    /**
     * The test result.
     * 1: negative
     * 2: positive
     * 3: invalid
     * 4: redeemed
     * 5: quick-test-Pending
     * 6: quick-test-Negative
     * 7: quick-test-Positive
     * 8: quick-test-Invalid
     * 9: quick-test-Redeemed
     */
    @Min(1)
    @Max(3)
    @NotNull
    @Schema(description = "the result of the PCR test", required = true)
    private Short result;

    /**
     * Timestamp of the SampleCollection (sc).
     */
    private Long sc;

    /**
     * The lab id.
     */
    private String labId;

    public PcrTestResult setId(String id) {
        this.id = id;
        return this;
    }

    public PcrTestResult setResult(Short result) {
        this.result = result;
        return this;
    }

    public PcrTestResult setSampleCollection(Long sc) {
        this.sc = sc;
        return this;
    }
}
