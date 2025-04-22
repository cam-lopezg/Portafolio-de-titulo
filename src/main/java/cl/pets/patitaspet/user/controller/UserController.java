package cl.pets.patitaspet.user.controller;

import cl.pets.patitaspet.user.dto.UserRegisterRequest;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
