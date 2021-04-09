package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.service.QuickTestServiceException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Utilities {

    @Value("${quick-test.tenantIdKey}")
    String tenantIdKey;
    @Value("${quick-test.tenantSpotIdKey}")
    String tenantSpotIdKey;
    @Value("${quick-test.testSpotIdName}")
    String testSpotIdName;

    /**
     * Get tenantID and testspotID from Token.
     * @return Map with tokens from keycloak (tenantID and testspotID)
     * @throws QuickTestServiceException TenantID or testSpotId not found
     */
    public Map<String, String> getIdsFromToken() throws QuickTestServiceException {

        Map<String, String> ids = new HashMap<>();

        KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken)
          SecurityContextHolder.getContext().getAuthentication();

        Principal principal =  authentication != null ? (Principal) authentication.getPrincipal() : null;

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            ids.put(tenantIdKey, keycloakPrincipal.getKeycloakSecurityContext().getRealm());
            IDToken token = keycloakPrincipal.getKeycloakSecurityContext().getToken();

            Map<String, Object> customClaims = token.getOtherClaims();

            if (customClaims.containsKey(testSpotIdName)) {
                ids.put(tenantSpotIdKey, String.valueOf(customClaims.get(testSpotIdName)));
            }
        }
        if (!ids.containsKey(tenantIdKey) && !ids.containsKey(tenantSpotIdKey)) {
            log.debug("Ids not found in User-Token");
            throw new QuickTestServiceException(QuickTestServiceException.Reason.INSERT_CONFLICT);
        }
        return ids;
    }
}
