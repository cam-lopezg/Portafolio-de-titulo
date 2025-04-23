package cl.pets.patitaspet.user.service;

import cl.pets.patitaspet.common.util.JwtTokenUtil;
import cl.pets.patitaspet.common.util.PasswordEncoder;
import cl.pets.patitaspet.user.dto.UserLoginRequest;
import cl.pets.patitaspet.user.dto.UserLoginResponse;
import cl.pets.patitaspet.user.dto.UserRegisterRequest;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.entity.UserLogin;
import cl.pets.patitaspet.user.repository.FirestoreUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final FirestoreUserRepository firestoreUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public UserServiceImpl(FirestoreUserRepository firestoreUserRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenUtil jwtTokenUtil) {
        this.firestoreUserRepository = firestoreUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
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
        // Cifrar la contraseña antes de almacenarla
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setName(request.getName());
        // Usa el nuevo método para establecer la fecha como string formateado
        newUser.setCreatedAtFromDateTime(LocalDateTime.now());
        newUser.setPets(new ArrayList<>());

        // Guardar usuario en Firebase Firestore
        String userId = firestoreUserRepository.saveUser(newUser);
        System.out.println("Usuario registrado en Firebase con ID: " + userId);
    }

    @Override
    public UserLoginResponse loginUser(UserLoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de login no puede ser nula.");
        }

        String email = request.getEmail();
        String password = request.getPassword();

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }

        // Buscar usuario por email
        Optional<User> userOpt = firestoreUserRepository.findUserByEmail(email);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("El usuario no existe.");
        }

        User user = userOpt.get();

        // Verificar contraseña usando el password encoder
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Contraseña incorrecta.");
        }

        // Crear registro de login exitoso
        UserLogin loginRecord = new UserLogin();
        loginRecord.setUser(user);
        loginRecord.setLoginTime(LocalDateTime.now());
        loginRecord.setSuccessful(true);

        // Aquí podrías guardar el registro de login en Firestore si lo necesitas

        // Generar token JWT
        String token = jwtTokenUtil.generateToken(user);

        // Crear y devolver la respuesta con el token
        UserLoginResponse response = new UserLoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail());
        response.setToken(token);

        return response;
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}