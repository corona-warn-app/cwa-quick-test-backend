package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import lombok.Data;

@Schema(
    description = "The quick test tenant statistics model."
)
@Data
public class QuickTestTenantStatisticsResponse {
    Aggregation aggregation;
    QuickTestStatisticsResponse quickTestStatisticsResponse;
    String pocId;
    ZonedDateTime timestamp;
}
