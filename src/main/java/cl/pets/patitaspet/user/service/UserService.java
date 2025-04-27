package cl.pets.patitaspet.user.service;

import cl.pets.patitaspet.user.dto.UserLoginRequest;
import cl.pets.patitaspet.user.dto.UserLoginResponse;
import cl.pets.patitaspet.user.dto.UserRegisterRequest;
import cl.pets.patitaspet.user.dto.UserUpdateRequest;
import cl.pets.patitaspet.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    void registerUser(UserRegisterRequest request);

    UserLoginResponse loginUser(UserLoginRequest request);

    // Métodos nuevos para manejo de imágenes de perfil
    User updateProfileImage(Long userId, MultipartFile imageFile) throws IOException;

    String getProfileImageUrl(Long userId);

    // Método nuevo para actualizar perfil de usuario
    User updateUserProfile(Long userId, UserUpdateRequest request);

    // Método para obtener información del usuario actual
    User getUserById(Long userId);
}