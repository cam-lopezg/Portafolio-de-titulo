package cl.pets.patitaspet.common.config;

import cl.pets.patitaspet.common.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desactivar CSRF para API REST
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No usar sesiones
                )
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (no requieren autenticación)
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        // Permitir acceso a los endpoints para subir imágenes (solo para pruebas)
                        .requestMatchers("/api/pets/with-image").permitAll()
                        .requestMatchers("/api/pets/{id}/image").permitAll()
                        .requestMatchers("/api/users/{id}/profile-image").permitAll()
                        // Permitir acceso a recursos estáticos
                        .requestMatchers("/uploads/**").permitAll()
                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated());

        // Añadir nuestro filtro JWT antes del filtro
        // UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}