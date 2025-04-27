package cl.pets.patitaspet.pet.controller;

import cl.pets.patitaspet.pet.dto.PetMedicationRequest;
import cl.pets.patitaspet.pet.dto.PetMedicationResponse;
import cl.pets.patitaspet.pet.service.PetMedicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/pets/{petId}/medications")
public class PetMedicationController {

    private static final Logger logger = Logger.getLogger(PetMedicationController.class.getName());

    @Autowired
    private PetMedicationService petMedicationService;

    /**
     * Registra un nuevo medicamento para una mascota
     */
    @PostMapping
    public ResponseEntity<?> createMedication(
            @PathVariable Long petId,
            @RequestBody PetMedicationRequest request) {

        try {
            logger.info("Solicitando creación de medicamento para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<PetMedicationResponse> future = petMedicationService.createMedication(petId, request,
                    userEmail);

            // Esperar a que se complete la operación
            PetMedicationResponse response = future.get();
            logger.info("Medicamento creado exitosamente para mascota: " + petId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al crear medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al crear medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al crear medicamento: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al crear medicamento: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al crear medicamento: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al crear medicamento: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al crear medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear medicamento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene todos los medicamentos de una mascota
     */
    @GetMapping
    public ResponseEntity<?> getMedications(@PathVariable Long petId) {
        try {
            logger.info("Solicitando lista de medicamentos para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<List<PetMedicationResponse>> future = petMedicationService.getMedicationsByPet(petId,
                    userEmail);

            // Esperar a que se complete la operación
            List<PetMedicationResponse> response = future.get();
            logger.info("Se encontraron " + response.size() + " medicamentos para la mascota: " + petId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al listar medicamentos: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al listar medicamentos: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al listar medicamentos: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al listar medicamentos: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al listar medicamentos: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al listar medicamentos: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al listar medicamentos: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al listar medicamentos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene un medicamento específico
     */
    @GetMapping("/{medicationId}")
    public ResponseEntity<?> getMedication(
            @PathVariable Long petId,
            @PathVariable Long medicationId) {

        try {
            logger.info("Solicitando información de medicamento: " + medicationId + " para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<PetMedicationResponse> future = petMedicationService.getMedication(medicationId,
                    petId, userEmail);

            // Esperar a que se complete la operación
            PetMedicationResponse response = future.get();
            logger.info("Medicamento encontrado para mascota: " + petId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al obtener medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al obtener medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al obtener medicamento: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al obtener medicamento: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al obtener medicamento: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al obtener medicamento: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al obtener medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener medicamento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Actualiza un medicamento específico
     */
    @PutMapping("/{medicationId}")
    public ResponseEntity<?> updateMedication(
            @PathVariable Long petId,
            @PathVariable Long medicationId,
            @RequestBody PetMedicationRequest request) {

        try {
            logger.info("Solicitando actualización de medicamento: " + medicationId + " para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<PetMedicationResponse> future = petMedicationService.updateMedication(medicationId, petId,
                    request, userEmail);

            // Esperar a que se complete la operación
            PetMedicationResponse response = future.get();
            logger.info("Medicamento actualizado exitosamente para mascota: " + petId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al actualizar medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al actualizar medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al actualizar medicamento: " + cause.getMessage(),
                        cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al actualizar medicamento: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al actualizar medicamento: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al actualizar medicamento: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al actualizar medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al actualizar medicamento: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Elimina un medicamento específico
     */
    @DeleteMapping("/{medicationId}")
    public ResponseEntity<?> deleteMedication(
            @PathVariable Long petId,
            @PathVariable Long medicationId) {

        try {
            logger.info("Solicitando eliminación de medicamento: " + medicationId + " para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<Boolean> future = petMedicationService.deleteMedication(medicationId, petId, userEmail);

            // Esperar a que se complete la operación
            boolean success = future.get();
            logger.info("Medicamento eliminado exitosamente para mascota: " + petId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", "Medicamento eliminado correctamente");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al eliminar medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al eliminar medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al eliminar medicamento: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al eliminar medicamento: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al eliminar medicamento: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al eliminar medicamento: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al eliminar medicamento: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al eliminar medicamento: " + e.getMessage());
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