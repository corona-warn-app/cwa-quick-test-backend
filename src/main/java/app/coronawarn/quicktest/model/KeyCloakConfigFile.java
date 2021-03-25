package app.coronawarn.quicktest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Schema(
    description = "Configuration Model for KeyCloak Clients"
)
@Data
@RequiredArgsConstructor
public class KeyCloakConfigFile {

    private final String realm;

    @JsonProperty("auth-server-url")
    private final String authServerUrl;

    @JsonProperty("ssl-required")
    private final String sslRequired = "external";

    private final String resource = "quick-test-portal";

    @JsonProperty("public-client")
    private final boolean publicClient = true;

}
