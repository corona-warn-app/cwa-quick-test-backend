package app.coronawarn.quicktest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
@Schema(
  description = "The quick test update ."
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickTestUpdateRequest {

  @NonNull
  private String guid;

  @NonNull
  private TestResult result;

}
