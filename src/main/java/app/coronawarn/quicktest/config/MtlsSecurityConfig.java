/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.quicktest.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.preauth.x509.X509PrincipalExtractor;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(name = "server.ssl.client-auth", havingValue = "need")
public class MtlsSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
    private final QuickTestConfig quickTestConfig;
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_LAB = "ROLE_c19_quick_test_lab";
    public static final String ROLE_COUNTER = "ROLE_c19_quick_test_counter";
    public static final String ROLE_ADMIN = "ROLE_c19_quick_test_admin";
    public static final String ROLE_TENANT_COUNTER = "ROLE_c19_quick_tenant_test_counter";

    private static final String API_ROUTE = "/api/**";
    private static final String CONFIG_ROUTE = "/api/config/*";
    private static final String SAMESITE_LAX = "Lax";
    private static final String OAUTH_TOKEN_REQUEST_STATE_COOKIE = "OAuth_Token_Request_State";

    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver(KeycloakSpringBootProperties properties) {
        return new QuicktestKeycloakSpringBootConfigResolver(properties);
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
            .x509().x509PrincipalExtractor(new
                        ThumbprintX509PrincipalExtractor()).userDetailsService(userDetailsService())
            .and().authorizeRequests()
            .mvcMatchers(HttpMethod.GET, CONFIG_ROUTE).permitAll()
            .mvcMatchers(HttpMethod.GET, API_ROUTE).authenticated()
            .mvcMatchers(HttpMethod.POST, API_ROUTE).authenticated()
            .mvcMatchers(HttpMethod.PUT, API_ROUTE).authenticated()
            .mvcMatchers(HttpMethod.PATCH, API_ROUTE).authenticated()
            .mvcMatchers(HttpMethod.DELETE, API_ROUTE).authenticated();
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

    @Override
    public UserDetailsService userDetailsService() {
        return hash -> {

            boolean allowed = Stream.of(quickTestConfig.getAllowedClientCertificates()
                            .split(","))
                    .map(String::trim)
                    .anyMatch(entry -> entry.equalsIgnoreCase(hash));

            if (allowed) {
                return new User(hash, "", Collections.emptyList());
            } else {
                log.error("Failed to authenticate cert with hash {}", hash);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private static class ThumbprintX509PrincipalExtractor implements X509PrincipalExtractor {

        MessageDigest messageDigest;

        private ThumbprintX509PrincipalExtractor() throws NoSuchAlgorithmException {
            messageDigest = MessageDigest.getInstance("SHA-256");
        }

        @Override
        public Object extractPrincipal(X509Certificate x509Certificate) {
            try {
                return String.valueOf(Hex.encode(messageDigest.digest(x509Certificate.getEncoded())));
            } catch (CertificateEncodingException e) {
                log.error("Failed to extract bytes from certificate");
                return null;
            }
        }
    }
}
