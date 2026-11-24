package com.conicream.alpaso.infrastructure.security;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity // <-- ACTIVA LAS ANOTACIONES @PreAuthorize EN LOS CONTROLADORES
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    JwtTokenValidator jwtTokenValidator(JwtProperties propiedades, Clock reloj) {
        return new JwtTokenValidator(propiedades.secretBase64(), reloj);
    }

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, JwtTokenValidator validador) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                // 1. Catálogo: Ver = Comerciante o Administrador; Modificar = Solo Administrador
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/v1/productos/**")
                    .hasAnyRole("COMERCIANTE", "ADMINISTRADOR", "USER", "ADMIN")
                .requestMatchers("/api/productos/**", "/api/v1/productos/**")
                    .hasAnyRole("ADMINISTRADOR", "ADMIN")

                // 2. Pedidos y Liquidación: Comerciantes y Administradores
                .requestMatchers("/api/pedidos/**", "/api/v1/pedidos/**")
                    .hasAnyRole("COMERCIANTE", "ADMINISTRADOR", "USER", "ADMIN")

                // 3. Reportes oficiales (Excel / Métricas): Solo Administrador
                .requestMatchers("/api/reportes/**", "/api/v1/reportes/**")
                    .hasAnyRole("ADMINISTRADOR", "ADMIN")

                .anyRequest().denyAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> escribir(res, 401, "No autenticado: falta un token válido de Coni-Cream"))
                .accessDeniedHandler((req, res, ex) -> escribir(res, 403, "No tiene permisos para esta operación")))
            .addFilterBefore(new JwtAuthenticationFilter(validador), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void escribir(jakarta.servlet.http.HttpServletResponse res, int estado, String mensaje)
            throws java.io.IOException {
        res.setStatus(estado);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + estado + ",\"mensaje\":\"" + mensaje + "\"}");
    }
}
