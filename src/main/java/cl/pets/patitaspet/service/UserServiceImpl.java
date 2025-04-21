package cl.pets.patitaspet.service;

import cl.pets.patitaspet.model.UserRegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public void registerUser(UserRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud no puede ser nula.");
        }

        String email = request.getEmail();
        String password = request.getPassword();

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío.");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("El formato de email es inválido.");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }

        // Simular "guardar" en base de datos
        System.out.println("Registrando usuario: " + email);
        System.out.println("password: " + password);
    }

    private boolean isValidEmail(String email) {
        // Validación básica
        return email.contains("@") && email.contains(".");
    }
}
