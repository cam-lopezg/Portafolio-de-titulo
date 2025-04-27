package cl.pets.patitaspet.pet.controller;

import cl.pets.patitaspet.pet.dto.PetExpenseRequest;
import cl.pets.patitaspet.pet.dto.PetExpenseResponse;
import cl.pets.patitaspet.pet.entity.ExpenseCategory;
import cl.pets.patitaspet.pet.service.PetExpenseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/pets/{petId}/expenses")
public class PetExpenseController {

    private static final Logger logger = Logger.getLogger(PetExpenseController.class.getName());

    @Autowired
    private PetExpenseService petExpenseService;

    /**
     * Registra un nuevo gasto para una mascota
     */
    @PostMapping
    public ResponseEntity<?> createExpense(
            @PathVariable Long petId,
            @RequestBody PetExpenseRequest request) {

        try {
            logger.info("Solicitando creación de gasto para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<PetExpenseResponse> future = petExpenseService.createExpense(petId, request,
                    userEmail);

            // Esperar a que se complete la operación
            PetExpenseResponse response = future.get();
            logger.info("Gasto creado exitosamente para mascota: " + petId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al crear gasto: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al crear gasto: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al crear gasto: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al crear gasto: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al crear gasto: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al crear el gasto: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al crear gasto: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear el gasto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene todos los gastos de una mascota
     */
    @GetMapping
    public ResponseEntity<?> getExpenses(@PathVariable Long petId) {
        try {
            logger.info("Solicitando todos los gastos para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<List<PetExpenseResponse>> future = petExpenseService.getExpensesByPet(petId,
                    userEmail);

            // Esperar a que se complete la operación
            List<PetExpenseResponse> responses = future.get();
            logger.info("Se encontraron " + responses.size() + " gastos para la mascota: " + petId);

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al buscar gastos: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al buscar gastos: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al buscar gastos: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al buscar gastos: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al buscar gastos: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al buscar los gastos: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar gastos: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al buscar los gastos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene los gastos de una mascota filtrados por categoría
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getExpensesByCategory(
            @PathVariable Long petId,
            @PathVariable ExpenseCategory category) {
        try {
            logger.info("Solicitando gastos por categoría " + category + " para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<List<PetExpenseResponse>> future = petExpenseService.getExpensesByPetAndCategory(petId,
                    category, userEmail);

            // Esperar a que se complete la operación
            List<PetExpenseResponse> responses = future.get();
            logger.info("Se encontraron " + responses.size() + " gastos de categoría " + category + " para la mascota: "
                    + petId);

            return ResponseEntity.ok(responses);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al buscar gastos por categoría: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al buscar gastos por categoría: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al buscar gastos por categoría: " + cause.getMessage(),
                        cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al buscar gastos por categoría: " + cause.getMessage(),
                        cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al buscar gastos por categoría: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al buscar los gastos por categoría: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar gastos por categoría: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al buscar los gastos por categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene un gasto específico
     */
    @GetMapping("/{expenseId}")
    public ResponseEntity<?> getExpense(
            @PathVariable Long petId,
            @PathVariable Long expenseId) {

        try {
            logger.info("Solicitando información de gasto: " + expenseId + " para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<PetExpenseResponse> future = petExpenseService.getExpense(expenseId, petId, userEmail);

            // Esperar a que se complete la operación
            PetExpenseResponse response = future.get();
            logger.info("Gasto encontrado para mascota: " + petId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al buscar gasto: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al buscar gasto: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al buscar gasto: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al buscar gasto: " + cause.getMessage(), cause);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al buscar gasto: " + e.getMessage(), e);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Error al buscar el gasto: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar gasto: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al buscar el gasto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Elimina un gasto
     */
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteExpense(
            @PathVariable Long petId,
            @PathVariable Long expenseId) {

        try {
            logger.info("Solicitando eliminación de gasto: " + expenseId + " para mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<Boolean> future = petExpenseService.deleteExpense(expenseId, petId, userEmail);

            // Esperar a que se complete la operación
            Boolean deleted = future.get();
            logger.info("Gasto eliminado con éxito: " + expenseId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Gasto eliminado con éxito");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al eliminar gasto: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al eliminar gasto: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al eliminar gasto: " + cause.getMessage(), cause);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al eliminar gasto: " + cause.getMessage(), cause);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al eliminar gasto: " + e.getMessage(), e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Error al eliminar el gasto: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al eliminar gasto: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar el gasto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Sube una imagen de recibo para un gasto
     */
    @PostMapping(value = "/{expenseId}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadReceiptImage(
            @PathVariable Long petId,
            @PathVariable Long expenseId,
            @RequestParam("image") MultipartFile image) {
        try {
            logger.info("Solicitando subir imagen de recibo para gasto: " + expenseId + " de mascota: " + petId);
            String userEmail = getCurrentUserEmail();

            CompletableFuture<String> future = petExpenseService.uploadReceiptImage(expenseId, petId, userEmail, image);

            // Esperar a que se complete la operación
            String imageUrl = future.get();
            logger.info("Imagen de recibo subida exitosamente: " + imageUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("expenseId", expenseId);
            response.put("receiptImageUrl", imageUrl);
            response.put("message", "Imagen de recibo subida exitosamente");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al subir imagen de recibo: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al subir imagen de recibo: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                logger.log(Level.WARNING, "Error de validación al subir imagen de recibo: " + cause.getMessage(),
                        cause);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", cause.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            } else if (cause instanceof AccessDeniedException) {
                logger.log(Level.WARNING, "Acceso denegado al subir imagen de recibo: " + cause.getMessage(), cause);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", cause.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            } else {
                logger.log(Level.SEVERE, "Error inesperado al subir imagen de recibo: " + e.getMessage(), e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Error al subir imagen de recibo: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al subir imagen de recibo: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al subir imagen de recibo: " + e.getMessage());
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