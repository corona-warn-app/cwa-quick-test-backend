package app.coronawarn.quicktest.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


/**
 * This class represents the TestResult.
 *
 * @see <a href="https://github.com/corona-warn-app/cwa-testresult-server/blob/master/docs/architecture-overview.md#core-entities">Core Entities</a>
 */
@Schema(
  description = "The test result model."
)
@Data
@AllArgsConstructor
public class TestResult {
  @NonNull
  private int testResult;

  @NonNull
  private String personalDataHash;
}
