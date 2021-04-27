package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(
        description = "Model for quicktest response."
)
@Data
public class QuickTestResponse {

    private String shortHashedGuid;

}
