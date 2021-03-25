package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;

@Schema(
    description = "The quick test creation model."
)
@Data
@RequiredArgsConstructor
public class QuicktestCreationRequest {

    private final String guid;

    private final String personalDataHash;
}
