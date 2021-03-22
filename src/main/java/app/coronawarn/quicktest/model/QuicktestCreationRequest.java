package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Schema(
    description = "The quick test creation model."
)
@Data
@AllArgsConstructor
public class QuicktestCreationRequest {

    @NonNull
    private String guid;

    @NonNull
    private String personalDataHash;
}
