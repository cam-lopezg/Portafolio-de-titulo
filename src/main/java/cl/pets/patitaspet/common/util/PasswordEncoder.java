package cl.pets.patitaspet.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Utilidad para cifrar contraseñas y verificar contraseñas cifradas
 */
@Component
public class PasswordEncoder {

    private final BCryptPasswordEncoder encoder;

    public PasswordEncoder() {
        // El parámetro strength determina la complejidad del cifrado (valor
        // recomendado: 10-12)
        this.encoder = new BCryptPasswordEncoder(12);
    }

    /**
     * Cifra una contraseña utilizando BCrypt
     * 
     * @param rawPassword La contraseña en texto plano
     * @return La contraseña cifrada
     */
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía");
        }
        return encoder.encode(rawPassword);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con una contraseña cifrada
     * 
     * @param rawPassword     La contraseña en texto plano a verificar
     * @param encodedPassword La contraseña cifrada almacenada
     * @return true si la contraseña coincide, false en caso contrario
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }
}