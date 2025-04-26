package cl.pets.patitaspet.pet.controller;

import cl.pets.patitaspet.pet.dto.PetVaccinationRequest;
import cl.pets.patitaspet.pet.dto.PetVaccinationResponse;
import cl.pets.patitaspet.pet.service.PetVaccinationService;

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
@RequestMapping("/api/pets/{petId}/vaccinations")
public class PetVaccinationController {

    private static final Logger logger = Logger.getLogger(PetVaccinationController.class.getName());

    @Autowired
    private PetVaccinationService petVaccinationService;

    /**
     * Registra una nueva vacunación para una mascota
     */
    @PostMapping
    public ResponseEntity<?> createVaccination(
            @PathVariable Long petId,
            @RequestBody PetVaccinationRequest request) {

        try {
            logger.info("Solicitando creación de vacunación para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<PetVaccinationResponse> future = petVaccinationService.createVaccination(petId, request,
                    userEmail);

            // Esperar a que se complete la operación
            PetVaccinationResponse response = future.get();
            logger.info("Vacunación creada exitosamente para mascota: " + petId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al crear vacunación: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al crear vacunación: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al crear vacunación: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al crear vacunación: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al crear vacunación: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al crear la vacunación: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al crear vacunación: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear la vacunación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene todas las vacunaciones de una mascota
     */
    @GetMapping
    public ResponseEntity<?> getVaccinations(@PathVariable Long petId) {
        try {
            logger.info("Solicitando todas las vacunaciones para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<List<PetVaccinationResponse>> future = petVaccinationService.getVaccinationsByPet(petId,
                    userEmail);

            // Esperar a que se complete la operación
            List<PetVaccinationResponse> responses = future.get();
            logger.info("Se encontraron " + responses.size() + " vacunaciones para la mascota: " + petId);

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al buscar vacunaciones: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al buscar vacunaciones: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al buscar vacunaciones: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al buscar vacunaciones: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al buscar vacunaciones: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al buscar las vacunaciones: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar vacunaciones: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al buscar las vacunaciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene una vacunación específica
     */
    @GetMapping("/{vaccinationId}")
    public ResponseEntity<?> getVaccination(
            @PathVariable Long petId,
            @PathVariable Long vaccinationId) {

        try {
            logger.info("Solicitando información de vacunación: " + vaccinationId + " para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<PetVaccinationResponse> future = petVaccinationService.getVaccination(vaccinationId,
                    petId, userEmail);

            // Esperar a que se complete la operación
            PetVaccinationResponse response = future.get();
            logger.info("Vacunación encontrada para mascota: " + petId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al buscar vacunación: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al buscar vacunación: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al buscar vacunación: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al buscar vacunación: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al buscar vacunación: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al buscar la vacunación: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar vacunación: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al buscar la vacunación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Elimina una vacunación
     */
    @DeleteMapping("/{vaccinationId}")
    public ResponseEntity<?> deleteVaccination(
            @PathVariable Long petId,
            @PathVariable Long vaccinationId) {

        try {
            logger.info("Solicitando eliminación de vacunación: " + vaccinationId + " para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<Boolean> future = petVaccinationService.deleteVaccination(vaccinationId, petId,
                    userEmail);

            // Esperar a que se complete la operación
            Boolean deleted = future.get();
            logger.info("Vacunación eliminada con éxito: " + vaccinationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vacunación eliminada con éxito");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al eliminar vacunación: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al eliminar vacunación: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al eliminar vacunación: " + cause.getMessage(), cause);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al eliminar vacunación: " + cause.getMessage(), cause);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al eliminar vacunación: " + e.getMessage(), e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Error al eliminar la vacunación: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al eliminar vacunación: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar la vacunación: " + e.getMessage());
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