package cl.pets.patitaspet.user.controller;

import cl.pets.patitaspet.user.dto.UserLoginRequest;
import cl.pets.patitaspet.user.dto.UserLoginResponse;
import cl.pets.patitaspet.user.dto.UserRegisterRequest;
import cl.pets.patitaspet.user.dto.UserUpdateRequest;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<String> holaMundo() {
        return ResponseEntity.ok("Hola Mundo");
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createRequest(@RequestBody String request) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Petición recibida");
        response.put("data", request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        try {
            userService.registerUser(userRegisterRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente en Firebase");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al registrar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest userLoginRequest) {
        try {
            UserLoginResponse response = userService.loginUser(userLoginRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error durante el login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint para subir o actualizar la imagen de perfil de un usuario
     * 
     * @param userId ID del usuario
     * @param image  Archivo de imagen
     * @return Respuesta con la URL de la imagen
     */
    @PostMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile image) {
        try {
            // Verificación básica del archivo de imagen
            if (image == null || image.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "No se ha proporcionado ninguna imagen");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Verificación del tipo de contenido
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "El archivo debe ser una imagen válida (tipo: " + contentType + ")");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            User updatedUser = userService.updateProfileImage(userId, image);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("photoUrl", updatedUser.getPhotoUrl());
            response.put("message", "Imagen de perfil actualizada exitosamente");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al procesar la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            // Capturar cualquier otra excepción no manejada
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error inesperado: " + e.getMessage());
            e.printStackTrace(); // Imprime el stack trace para depuración
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint para obtener la URL de la imagen de perfil de un usuario
     * 
     * @param userId ID del usuario
     * @return URL de la imagen de perfil
     */
    @GetMapping("/{userId}/profile-image")
    public ResponseEntity<?> getProfileImage(@PathVariable Long userId) {
        try {
            String photoUrl = userService.getProfileImageUrl(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("photoUrl", photoUrl);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint para obtener la información de un usuario
     * 
     * @param userId ID del usuario
     * @return Información completa del usuario
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);

            // Omitir información sensible como la contraseña hash
            user.setPasswordHash(null);

            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener información del usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Endpoint para actualizar la información de un usuario
     * 
     * @param userId  ID del usuario
     * @param request Datos de actualización del usuario
     * @return Usuario actualizado
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {
        try {
            User updatedUser = userService.updateUserProfile(userId, request);

            // Omitir información sensible como la contraseña hash
            updatedUser.setPasswordHash(null);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Perfil actualizado exitosamente");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al actualizar el perfil: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
