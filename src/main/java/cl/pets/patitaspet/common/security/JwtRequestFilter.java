package cl.pets.patitaspet.common.security;

import cl.pets.patitaspet.common.util.JwtTokenUtil;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.repository.FirestoreUserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private FirestoreUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String email = null;
        String jwtToken = null;

        // JWT token está en el formato "Bearer token". Removemos el prefijo Bearer
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                email = jwtTokenUtil.getEmailFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.error("No se pudo obtener el token JWT");
            } catch (ExpiredJwtException e) {
                logger.error("El token JWT ha expirado");
            } catch (MalformedJwtException e) {
                logger.error("Token JWT inválido");
            }
        } else {
            logger.warn("El JWT Token no comienza con Bearer o no está presente");
        }

        // Una vez que tengamos el token, validamos y establecemos la autenticación
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            Optional<User> userOpt = userRepository.findUserByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Si el token es válido, configuramos Spring Security
                if (jwtTokenUtil.validateToken(jwtToken, email)) {

                    // Creamos un objeto UserDetails simple con los permisos básicos
                    // En una implementación más compleja, aquí se cargarían los roles del usuario
                    UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                            email, user.getPasswordHash(), new ArrayList<>());

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establecemos la autenticación en el contexto
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        }
        chain.doFilter(request, response);
    }
}