package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(
        description = ""
)
@Data
public class QuickTestStatistics {
    private Integer totalTestCount;
    private Integer positiveTestCount;
}
