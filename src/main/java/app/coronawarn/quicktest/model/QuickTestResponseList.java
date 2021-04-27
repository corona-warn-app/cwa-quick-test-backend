package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Schema(
        description = "Model for quicktest response list."
)
@Getter
@ToString
@EqualsAndHashCode
public class QuickTestResponseList {

    @NotNull
    private List<@Valid QuickTestResponse> quickTests;

    public QuickTestResponseList setQuickTests(List<QuickTestResponse> quickTests) {
        this.quickTests = quickTests;
        return this;
    }

}
