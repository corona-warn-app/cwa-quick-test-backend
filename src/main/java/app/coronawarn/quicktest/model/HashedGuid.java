package app.coronawarn.quicktest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents the hashed Guid.
 * Hash (SHA256) aka QR-Code, GUID encoded as hex string.
 *
 * @see <a href="https://github.com/corona-warn-app/cwa-testresult-server/blob/master/docs/architecture-overview.md#core-entities">Core Entities</a>
 */
@Schema(
    description = "The hashed Guid request model."
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HashedGuid {
    private String guidHash;
}
