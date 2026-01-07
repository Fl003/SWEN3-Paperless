package at.technikum.paperless.config;

import at.technikum.paperless.service.UserLoginService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.time.OffsetDateTime;

/*
 * Configuration class for authentication components
 *
 * This class defines beans(aka objects) that verify username and password
 * using Spring Security.
 */
@Slf4j
@Configuration
@EnableWebSecurity //integration spring-security
public class SecurityConfig {
    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private UserLoginService userLoginService;

    //filer for all requests -> for all who not login, make to login
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        //allowed only login page
                        .requestMatchers("/api/v1/login", "/error").permitAll()

                        //everything else closed
                        .anyRequest().authenticated())
                //no session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //this service checks jwt
                .oauth2ResourceServer((rs) -> rs
                        .jwt((jwt) -> jwt.decoder(jwtDecoder))
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        )

                .build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            log.warn("⚠️ Unauthorized access to {}: {}", request.getRequestURI(), authException.getMessage());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            String json = """
            {
              "timestamp": "%s",
              "status": 401,
              "error": "Unauthorized",
              "message": "%s",
              "path": "%s"
            }
            """.formatted(
                    OffsetDateTime.now(),
                    authException.getMessage(),
                    request.getRequestURI()
            );

            response.getWriter().write(json);
        };
    }
}
