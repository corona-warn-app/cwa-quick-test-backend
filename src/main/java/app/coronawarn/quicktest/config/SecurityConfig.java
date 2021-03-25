package app.coronawarn.quicktest.config;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    public static final String ROLE_LAB = "ROLE_c19_quick_test_lab";
    public static final String ROLE_COUNTER = "ROLE_c19_quick_test_counter";

    private static final String API_ROUTE = "/api/**";
    private static final String KEYCLOAK_CONFIG_ROUTE = "/api/config/keycloak.json";
    private static final String SAMESITE_LAX = "Lax";
    private static final String OAUTH_TOKEN_REQUEST_STATE_COOKIE = "OAuth_Token_Request_State";

    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    /**
     * Global Keycloak Configuration for CWA-Quick-Test-Backend.
     *
     * @param auth AuthenticationManagerBuilder
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
            .headers().addHeaderWriter(this::addSameSiteToOAuthCookie).and()
            .csrf().disable()
            .authorizeRequests()
            .mvcMatchers(HttpMethod.GET, KEYCLOAK_CONFIG_ROUTE).permitAll()
            .mvcMatchers(HttpMethod.GET, API_ROUTE).authenticated()
            .mvcMatchers(HttpMethod.POST, API_ROUTE).authenticated()
            .mvcMatchers(HttpMethod.PUT, API_ROUTE).authenticated();
    }

    private void addSameSiteToOAuthCookie(final HttpServletRequest request, final HttpServletResponse response) {
        final Collection<String> setCookieValues = response.getHeaders(HttpHeaders.SET_COOKIE);
        for (String setCookie : setCookieValues) {
            if (setCookie.contains(OAUTH_TOKEN_REQUEST_STATE_COOKIE)) {
                response.setHeader(HttpHeaders.SET_COOKIE, addSameSiteStrict(setCookie));
            }
        }
    }

    private String addSameSiteStrict(String setCookie) {
        return setCookie + "; SameSite=" + SAMESITE_LAX;
    }

}
