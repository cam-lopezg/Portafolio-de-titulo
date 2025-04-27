package cl.pets.patitaspet.user.service;

import cl.pets.patitaspet.common.service.FileStorageService;
import cl.pets.patitaspet.common.util.JwtTokenUtil;
import cl.pets.patitaspet.common.util.PasswordEncoder;
import cl.pets.patitaspet.user.dto.UserLoginRequest;
import cl.pets.patitaspet.user.dto.UserLoginResponse;
import cl.pets.patitaspet.user.dto.UserRegisterRequest;
import cl.pets.patitaspet.user.dto.UserUpdateRequest;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.entity.UserLogin;
import cl.pets.patitaspet.user.repository.FirestoreUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final FirestoreUserRepository firestoreUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final FileStorageService fileStorageService;

    @Autowired
    public UserServiceImpl(FirestoreUserRepository firestoreUserRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenUtil jwtTokenUtil,
            FileStorageService fileStorageService) {
        this.firestoreUserRepository = firestoreUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.fileStorageService = fileStorageService;
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

        // Asignar imagen por defecto al usuario
        newUser.setPhotoUrl("/uploads/defaults/default-user.png");

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

    @Override
    public User updateProfileImage(Long userId, MultipartFile imageFile) throws IOException {
        // Verificar que el archivo no sea nulo
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("La imagen no puede estar vacía");
        }

        // Validar que el archivo sea una imagen
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        // Buscar el usuario por su ID numérico (no por el ID del documento)
        Optional<User> userOpt = firestoreUserRepository.findUserByNumericId(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }

        User user = userOpt.get();

        try {
            // Si el usuario ya tiene una imagen de perfil, intentar eliminarla
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty() &&
                    !user.getPhotoUrl().contains("default-user.png")) {
                try {
                    fileStorageService.deleteFile(user.getPhotoUrl());
                } catch (Exception e) {
                    // Si falla la eliminación, solo logueamos el error pero continuamos con la
                    // actualización
                    System.out.println("Error al eliminar la imagen anterior: " + e.getMessage());
                }
            }

            // Guardar la nueva imagen en el directorio "users"
            String photoUrl = fileStorageService.storeFile(imageFile, "users");
            user.setPhotoUrl(photoUrl);

            // Actualizar el usuario en la base de datos
            firestoreUserRepository.updateUser(user);

            return user;
        } catch (IOException e) {
            throw new IOException("Error al procesar la imagen: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error inesperado al actualizar la imagen de perfil: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProfileImageUrl(Long userId) {
        // Buscar el usuario por su ID numérico
        Optional<User> userOpt = firestoreUserRepository.findUserByNumericId(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }

        User user = userOpt.get();

        // Si el usuario no tiene foto o es vacía, devolvemos la imagen por defecto
        if (user.getPhotoUrl() == null || user.getPhotoUrl().isEmpty()) {
            return "/uploads/defaults/default-user.png";
        }

        return user.getPhotoUrl();
    }

    @Override
    public User updateUserProfile(Long userId, UserUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de actualización no puede ser nula");
        }

        // Buscar el usuario por su ID numérico
        Optional<User> userOpt = firestoreUserRepository.findUserByNumericId(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }

        User user = userOpt.get();

        // Actualizar campos solo si están presentes en la solicitud
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            // Simple validación de número telefónico (solo dígitos, paréntesis, guiones y
            // espacios)
            if (!request.getPhoneNumber().matches("[\\d\\s\\(\\)\\-\\+]+")) {
                throw new IllegalArgumentException("Formato de número telefónico inválido");
            }
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        // Campos opcionales - si están vacíos, se mantienen como estaban
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim());
        }

        if (request.getBirthDate() != null && !request.getBirthDate().trim().isEmpty()) {
            try {
                // La validación se hace en el método setBirthDate de la entidad User
                user.setBirthDate(request.getBirthDate().trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Formato de fecha de nacimiento inválido. Use dd/MM/yyyy");
            }
        }

        // Guardar el usuario actualizado en la base de datos
        firestoreUserRepository.updateUser(user);

        return user;
    }

    @Override
    public User getUserById(Long userId) {
        // Buscar el usuario por su ID numérico
        Optional<User> userOpt = firestoreUserRepository.findUserByNumericId(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + userId);
        }

        return userOpt.get();
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}