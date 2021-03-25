package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Schema(
    description = "The quick test update ."
)
@Data
@AllArgsConstructor
public class QuickTestUpdateRequest {

    private final String guid;

    private final TestResult result;

}
