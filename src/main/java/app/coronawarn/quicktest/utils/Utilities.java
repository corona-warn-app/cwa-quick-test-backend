package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.service.QuickTestServiceException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Utilities {

    private final QuickTestConfig quickTestConfig;

    /**
     * Get tenantID and pocID from Token.
     *
     * @return Map with tokens from keycloak (tenantID and pocID)
     * @throws QuickTestServiceException TenantID or pocID not found
     */
    public Map<String, String> getIdsFromToken() throws QuickTestServiceException {

        Map<String, String> ids = new HashMap<>();

        KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken)
            SecurityContextHolder.getContext().getAuthentication();

        Principal principal = authentication != null ? (Principal) authentication.getPrincipal() : null;

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            ids.put(quickTestConfig.getTenantIdKey(), keycloakPrincipal.getKeycloakSecurityContext().getRealm());
            IDToken token = keycloakPrincipal.getKeycloakSecurityContext().getToken();

            Map<String, Object> customClaims = token.getOtherClaims();

            if (customClaims.containsKey(quickTestConfig.getPointOfCareIdName())) {
                ids.put(quickTestConfig.getTenantPointOfCareIdKey(),
                    String.valueOf(customClaims.get(quickTestConfig.getPointOfCareIdName())));
            }
        }
        if (!ids.containsKey(quickTestConfig.getTenantIdKey())
            && !ids.containsKey(quickTestConfig.getTenantPointOfCareIdKey())) {
            log.debug("Ids not found in User-Token");
            throw new QuickTestServiceException(QuickTestServiceException.Reason.INSERT_CONFLICT);
        }
        return ids;
    }
}
