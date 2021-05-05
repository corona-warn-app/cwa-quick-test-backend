package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(
    description = ""
)
@Data
@Builder
public class QuickTestStatistics {
    private Integer totalTestCount;
    private Integer positiveTestCount;
}
