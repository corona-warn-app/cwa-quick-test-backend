package app.coronawarn.quicktest;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String ACTUATOR_ROUTE = "/actuator/**";
    private static final String API_ROUTE = "/api/quicktest/**";
    private static final String FRONTEND_ROUTE = "/**";
    private static final String SWAGGER_ROUTE = "/v3/api-docs/**";
    private static final String SAMESITE_LAX = "Lax";
    private static final String OAUTH_TOKEN_REQUEST_STATE_COOKIE = "OAuth_Token_Request_State";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .headers().addHeaderWriter(this::addSameSiteToOAuthCookie)
            .and()
                .authorizeRequests()
                .mvcMatchers(HttpMethod.GET, ACTUATOR_ROUTE).permitAll()
                .mvcMatchers(HttpMethod.GET, API_ROUTE).permitAll()
                .mvcMatchers(HttpMethod.GET, FRONTEND_ROUTE).permitAll()
                .mvcMatchers(HttpMethod.GET, SWAGGER_ROUTE).permitAll()
                .anyRequest()
                .denyAll()
           .and()
                .csrf().ignoringAntMatchers(FRONTEND_ROUTE, SWAGGER_ROUTE);
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
