package com.example.demo.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.example.demo.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;

    public SecurityConfiguration(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http
            .cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz ->
                // prettier-ignore
                authz
                    .requestMatchers(new AntPathRequestMatcher("/api/authenticate", "POST")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/authenticate", "GET")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/register")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/activate")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/account/reset-password/init")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/account/reset-password/finish")).permitAll()
                    // Endpoints públicos de consulta de eventos (no requieren autenticación)
                    .requestMatchers(new AntPathRequestMatcher("/api/eventos/resumidos")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/eventos/{id}", "GET")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/eventos", "GET")).permitAll()
                    // Mapa de asientos y verificación de disponibilidad (públicos)
                    .requestMatchers(new AntPathRequestMatcher("/api/asientos/evento/*/mapa", "GET")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/asientos/evento/*/disponible", "GET")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                    // Todos los demás endpoints bajo /api/** requieren autenticación
                    .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated()
                    .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(new AntPathRequestMatcher("/management/health")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/management/health/**")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/management/info")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/management/prometheus")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/management/**")).hasAuthority(AuthoritiesConstants.ADMIN)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }
}
