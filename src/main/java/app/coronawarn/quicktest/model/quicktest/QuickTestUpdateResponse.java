package app.coronawarn.quicktest.model.quicktest;

import app.coronawarn.quicktest.model.demis.DemisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(
  description = "The quick test update response."
)
@Data
@AllArgsConstructor
public class QuickTestUpdateResponse {

    DemisStatus demisStatus;
    String details;
}
