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

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    JwtTokenValidator jwtTokenValidator(JwtProperties propiedades, Clock reloj) {
        return new JwtTokenValidator(propiedades.secretBase64(), reloj);
    }

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, JwtTokenValidator validador) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)                       // API sin sesión ni cookies
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                // Roles de AuthCore: USER = comerciante, ADMIN = administrador, BODEGUERO = repone stock.
                // El catálogo y el stock los puede VER cualquier usuario autenticado de estos tres roles.
                .requestMatchers(HttpMethod.GET, "/api/productos/**").hasAnyRole("USER", "ADMIN", "BODEGUERO")
                // El BODEGUERO solo puede reponer stock; va ANTES de la regla general de ADMIN.
                .requestMatchers(HttpMethod.POST, "/api/productos/*/stock/reposiciones").hasAnyRole("ADMIN", "BODEGUERO")
                // El resto de operaciones sobre productos (crear, editar, retirar, conteo manual) es solo ADMIN.
                .requestMatchers("/api/productos/**").hasRole("ADMIN")
                // Los pedidos y su liquidación son del comerciante; que sea SU pedido lo decide la capa de aplicación.
                .requestMatchers("/api/pedidos/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/reportes/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> escribir(res, 401, "No autenticado: falta un token válido"))
                .accessDeniedHandler((req, res, ex) -> escribir(res, 403, "No tiene permiso para esta operación")))
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
