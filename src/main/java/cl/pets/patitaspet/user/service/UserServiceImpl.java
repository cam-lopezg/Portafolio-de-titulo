package cl.pets.patitaspet.user.service;

import cl.pets.patitaspet.user.dto.UserRegisterRequest;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.repository.FirestoreUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class UserServiceImpl implements UserService {

    private final FirestoreUserRepository firestoreUserRepository;

    @Autowired
    public UserServiceImpl(FirestoreUserRepository firestoreUserRepository) {
        this.firestoreUserRepository = firestoreUserRepository;
    }

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

        // Para verificar si el usuario ya existe en Firebase
        if (firestoreUserRepository.findUserByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        // Crear nuevo usuario
        User newUser = new User();
        newUser.setEmail(email);
        // En una aplicación se deberia hashear la contraseña antes de guardarla
        newUser.setPasswordHash(password);
        newUser.setName(request.getName());
        // Usa el nuevo método para establecer la fecha como string formateado
        newUser.setCreatedAtFromDateTime(LocalDateTime.now());
        newUser.setPets(new ArrayList<>());

        // Guardar usuario en Firebase Firestore
        String userId = firestoreUserRepository.saveUser(newUser);
        System.out.println("Usuario registrado en Firebase con ID: " + userId);
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}