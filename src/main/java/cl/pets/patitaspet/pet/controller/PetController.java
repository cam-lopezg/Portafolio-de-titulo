package cl.pets.patitaspet.pet.controller;

import cl.pets.patitaspet.pet.dto.PetCreateRequest;
import cl.pets.patitaspet.pet.dto.PetResponse;
import cl.pets.patitaspet.pet.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private static final Logger logger = Logger.getLogger(PetController.class.getName());

    @Autowired
    private PetService petService;

    /**
     * Crea una nueva mascota asociada al usuario autenticado
     */
    @PostMapping
    public ResponseEntity<?> createPet(@RequestBody PetCreateRequest petCreateRequest) {
        try {
            logger.info("Iniciando creación de mascota: " + petCreateRequest.getName());

            // Obtener el email del usuario autenticado
            String userEmail = getCurrentUserEmail();
            logger.info("Usuario autenticado para crear mascota: " + userEmail);

            // Crear la mascota asociada al usuario actual
            PetResponse response = petService.createPet(petCreateRequest, userEmail);
            logger.info("Mascota creada exitosamente con ID: " + response.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al crear mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al crear mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear la mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene todas las mascotas del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<?> getPets() {
        try {
            logger.info("Solicitando listar todas las mascotas del usuario");

            // Obtener el email del usuario autenticado
            String userEmail = getCurrentUserEmail();
            logger.info("Usuario autenticado para listar mascotas: " + userEmail);

            // Obtener las mascotas del usuario
            List<PetResponse> pets = petService.getPetsByUser(userEmail);
            logger.info("Se encontraron " + pets.size() + " mascotas para el usuario");

            return ResponseEntity.ok(pets);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al listar mascotas: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al listar mascotas: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener las mascotas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene una mascota específica por ID, verificando que pertenezca al usuario
     * autenticado
     */
    @GetMapping("/{petId}")
    public ResponseEntity<?> getPetById(@PathVariable Long petId) {
        try {
            logger.info("Solicitando información de mascota con ID: " + petId);

            // Obtener el email del usuario autenticado
            String userEmail = getCurrentUserEmail();
            logger.info("Usuario autenticado para consulta de mascota: " + userEmail);

            // Obtener la mascota por ID (el servicio verifica que pertenezca al usuario)
            PetResponse pet = petService.getPetById(petId, userEmail);
            logger.info("Mascota encontrada: " + pet.getName() + " (ID: " + pet.getId() + ")");

            return ResponseEntity.ok(pet);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al buscar mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado a mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al obtener mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener la mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene el email del usuario actualmente autenticado
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.severe("Intento de acceso sin autenticación");
            throw new IllegalStateException("No hay usuario autenticado");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername(); // En nuestro caso, el username es el email
            logger.info("Usuario autenticado encontrado: " + email);
            return email;
        } else {
            String name = authentication.getName();
            logger.info("Usuario autenticado encontrado (no UserDetails): " + name);
            return name;
        }
    }
}