package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Data;

@Schema(
    description = "The quick test tenant statistics model."
)
@Data
@Builder
public class QuickTestTenantStatistics {
    Aggregation aggregation;
    QuickTestStatistics quickTestStatistics;
    String pocId;
    ZonedDateTime timestamp;
}
