package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.model.KeyCloakConfigFile;
import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class KeyCloakConfigController {

    private final KeycloakSpringBootProperties keycloakConfig;

    @GetMapping(value = "keycloak.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyCloakConfigFile> getKeyCloakConfig() {
        return ResponseEntity.ok(
            new KeyCloakConfigFile(keycloakConfig.getRealm(), keycloakConfig.getAuthServerUrl()));
    }
}
