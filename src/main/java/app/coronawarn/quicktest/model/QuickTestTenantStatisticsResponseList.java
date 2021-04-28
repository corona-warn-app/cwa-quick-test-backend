package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.ZonedDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Schema(
    description = "The quick test tenant statistics model list."
)

@Getter
@ToString
@EqualsAndHashCode
public class QuickTestTenantStatisticsResponseList {
    @NotNull
    private List<@Valid QuickTestTenantStatisticsResponse> quickTestTenantStatistics;

    public QuickTestTenantStatisticsResponseList setQuickTestTenantStatistics(
        List<QuickTestTenantStatisticsResponse> quickTestTenantStatistics) {
        this.quickTestTenantStatistics = quickTestTenantStatistics;
        return this;
    }
}
