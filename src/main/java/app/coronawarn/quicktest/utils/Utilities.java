package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.config.QuickTestConfig;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class Utilities {

    private final QuickTestConfig quickTestConfig;

    /**
     * Returns current utc datetime.
     */
    public static LocalDateTime getCurrentLocalDateTimeUtc() {
        return ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();
    }

    /**
     * Returns start datetime in Germany.
     */
    public static ZonedDateTime getStartTimeForLocalDateInGermanyInUtc() {
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("Europe/Berlin"))
            .with(ChronoField.NANO_OF_DAY, LocalTime.MIN.toNanoOfDay());
        return time.withZoneSameInstant(ZoneId.of("UTC"));
    }

    /**
     * Returns end datetime date in Germany.
     */
    public static ZonedDateTime getEndTimeForLocalDateInGermanyInUtc() {
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("Europe/Berlin"))
            .with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay());
        return time.withZoneSameInstant(ZoneId.of("UTC"));
    }

    /**
     * Get tenantID and pocID from Token.
     *
     * @return Map with tokens from keycloak (tenantID and pocID)
     * @throws ResponseStatusException 500 if Ids not found in User-Token
     */
    public Map<String, String> getIdsFromToken() throws ResponseStatusException {

        Map<String, String> ids = new HashMap<>();
        Principal principal = getPrincipal();

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
            || !ids.containsKey(quickTestConfig.getTenantPointOfCareIdKey())) {
            log.warn("Ids not found in User-Token");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ids;
    }

    /**
     * Get tenantID and pocID from Token.
     *
     * @return tenantID
     * @throws ResponseStatusException 500 if Id not found in User-Token
     */
    public String getTenantIdFromToken() throws ResponseStatusException {

        Map<String, String> ids = new HashMap<>();
        Principal principal = getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            return keycloakPrincipal.getKeycloakSecurityContext().getRealm();

        }
        log.warn("TenantID not found in User-Token");
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get tenantID and pocID from Token.
     *
     * @return Map with tokens from keycloak (tenantID and pocID)
     * @throws ResponseStatusException 500 if Poc Information not found in User-Token
     */
    public List<String> getPocInformationFromToken() throws ResponseStatusException {

        String information = null;
        Principal principal = getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            IDToken token = keycloakPrincipal.getKeycloakSecurityContext().getToken();
            Map<String, Object> customClaims = token.getOtherClaims();

            if (customClaims.containsKey(quickTestConfig.getPointOfCareInformationName())) {
                information = String.valueOf(customClaims.get(quickTestConfig.getPointOfCareInformationName()));
            }
        }
        if (information == null) {
            log.warn("Poc Information not found in User-Token");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return Arrays.asList(information.split(quickTestConfig.getPointOfCareInformationDelimiter()));
    }

    /**
     * Get tenantID and pocID from Token.
     *
     * @return Map with tokens from keycloak (tenantID and pocID)
     * @throws ResponseStatusException 500 Name not found in User-Token
     */
    public String getUserNameFromToken() throws ResponseStatusException {

        String name = null;
        Principal principal = getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) principal;
            IDToken token = keycloakPrincipal.getKeycloakSecurityContext().getToken();
            name = token.getName();
        }
        if (name == null) {
            log.warn("Name not found in User-Token");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return name;
    }

    private Principal getPrincipal() {
        KeycloakAuthenticationToken authentication = (KeycloakAuthenticationToken)
            SecurityContextHolder.getContext().getAuthentication();

        return authentication != null ? (Principal) authentication.getPrincipal() : null;
    }
}
