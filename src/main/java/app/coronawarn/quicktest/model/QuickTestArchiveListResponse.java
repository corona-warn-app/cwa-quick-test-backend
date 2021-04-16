package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Schema(
        description = ""
)
@Getter
@ToString
@EqualsAndHashCode
public class QuickTestArchiveListResponse {

    @NotNull
    private List<@Valid QuickTestArchiveResponse> quickTestArchives;

    public QuickTestArchiveListResponse setQuickTestArchives(List<QuickTestArchiveResponse> quickTestArchives) {
        this.quickTestArchives = quickTestArchives;
        return this;
    }

}
