package cl.pets.patitaspet.pet.service;

import cl.pets.patitaspet.common.service.FileStorageService;
import cl.pets.patitaspet.pet.dto.PetExpenseRequest;
import cl.pets.patitaspet.pet.dto.PetExpenseResponse;
import cl.pets.patitaspet.pet.entity.ExpenseCategory;
import cl.pets.patitaspet.pet.entity.Pet;
import cl.pets.patitaspet.pet.entity.PetExpense;
import cl.pets.patitaspet.pet.repository.FirestorePetRepository;
import cl.pets.patitaspet.pet.repository.PetExpenseRepository;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.repository.FirestoreUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PetExpenseServiceImpl implements PetExpenseService {

    private static final Logger logger = Logger.getLogger(PetExpenseServiceImpl.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private PetExpenseRepository expenseRepository;

    @Autowired
    private FirestorePetRepository petRepository;

    @Autowired
    private FirestoreUserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public CompletableFuture<PetExpenseResponse> createExpense(Long petId, PetExpenseRequest request,
            String userEmail) {
        logger.info("Iniciando creación de registro de gasto para mascota: " + petId);

        // CompletableFuture para el resultado
        CompletableFuture<PetExpenseResponse> future = new CompletableFuture<>();

        // Validar datos
        if (request == null) {
            logger.warning("Request de gasto es nulo");
            future.completeExceptionally(new IllegalArgumentException("La solicitud no puede ser nula"));
            return future;
        }

        if (petId == null) {
            logger.warning("ID de mascota es nulo");
            future.completeExceptionally(new IllegalArgumentException("El ID de la mascota no puede ser nulo"));
            return future;
        }

        // Validar título
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            logger.warning("Título de gasto vacío");
            future.completeExceptionally(new IllegalArgumentException("El título del gasto no puede estar vacío"));
            return future;
        }

        try {
            // Verificar que la mascota existe y pertenece al usuario
            logger.info("Buscando usuario: " + userEmail);
            Optional<User> userOpt = userRepository.findUserByEmail(userEmail);

            if (userOpt.isEmpty()) {
                logger.warning("No se encontró el usuario con email: " + userEmail);
                future.completeExceptionally(new IllegalArgumentException("Usuario no encontrado"));
                return future;
            }

            User user = userOpt.get();
            logger.info("Usuario encontrado con ID: " + user.getId());

            // Buscar la mascota por su ID numérico interno
            logger.info("Buscando mascota con ID numérico interno: " + petId);
            Optional<Pet> petOpt = petRepository.findPetByNumericId(petId);

            if (petOpt.isEmpty()) {
                logger.warning("No se encontró la mascota con ID numérico: " + petId);
                future.completeExceptionally(new IllegalArgumentException("Mascota no encontrada"));
                return future;
            }

            Pet pet = petOpt.get();
            logger.info("Mascota encontrada: " + pet.getName());

            // Verificar que la mascota pertenece al usuario autenticado
            if (!pet.getUserId().equals(user.getId())) {
                logger.warning("Intento de registro de gasto para mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para registrar gastos a esta mascota"));
                return future;
            }

            // Crear la entidad de gasto
            PetExpense expense = new PetExpense();
            expense.setPetId(petId);

            // Transferir datos del request
            expense.setTitle(request.getTitle());
            expense.setDescription(request.getDescription());
            expense.setAmount(request.getAmount());
            expense.setCurrencyCode(request.getCurrencyCode() != null ? request.getCurrencyCode() : "CLP");
            expense.setCategory(request.getCategory());
            expense.setDateStr(request.getDate());
            expense.setVendorName(request.getVendorName());

            // Establecer fecha de creación
            LocalDateTime now = LocalDateTime.now();
            expense.setCreatedAt(now);
            expense.setCreatedAtStr(now.format(DATETIME_FORMATTER));

            // Guardar el gasto
            logger.info("Guardando registro de gasto para mascota: " + petId);
            expenseRepository.save(expense)
                    .thenApply(savedExpense -> {
                        // Convertir a respuesta
                        PetExpenseResponse response = new PetExpenseResponse();
                        response.setId(savedExpense.getId());
                        response.setPetId(savedExpense.getPetId());
                        response.setPetName(pet.getName());

                        response.setTitle(savedExpense.getTitle());
                        response.setDescription(savedExpense.getDescription());
                        response.setAmount(savedExpense.getAmount());
                        response.setCurrencyCode(savedExpense.getCurrencyCode());
                        response.setCategory(savedExpense.getCategory());
                        response.setDate(savedExpense.getDateStr());
                        response.setVendorName(savedExpense.getVendorName());
                        response.setReceiptImageUrl(savedExpense.getReceiptImageUrl());
                        response.setCreatedAt(savedExpense.getCreatedAtStr());

                        logger.info("Gasto registrado con éxito. ID: " + savedExpense.getId());
                        future.complete(response);
                        return response;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al guardar gasto", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al registrar gasto", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<List<PetExpenseResponse>> getExpensesByPet(Long petId, String userEmail) {
        logger.info("Buscando gastos para mascota: " + petId);
        CompletableFuture<List<PetExpenseResponse>> future = new CompletableFuture<>();

        try {
            // Verificar que la mascota existe y pertenece al usuario
            logger.info("Buscando usuario: " + userEmail);
            Optional<User> userOpt = userRepository.findUserByEmail(userEmail);

            if (userOpt.isEmpty()) {
                logger.warning("No se encontró el usuario con email: " + userEmail);
                future.completeExceptionally(new IllegalArgumentException("Usuario no encontrado"));
                return future;
            }

            User user = userOpt.get();
            logger.info("Usuario encontrado con ID: " + user.getId());

            // Buscar la mascota por su ID numérico interno
            logger.info("Buscando mascota con ID numérico interno: " + petId);
            Optional<Pet> petOpt = petRepository.findPetByNumericId(petId);

            if (petOpt.isEmpty()) {
                logger.warning("No se encontró la mascota con ID numérico: " + petId);
                future.completeExceptionally(new IllegalArgumentException("Mascota no encontrada"));
                return future;
            }

            Pet pet = petOpt.get();
            logger.info("Mascota encontrada: " + pet.getName());

            // Verificar que la mascota pertenece al usuario autenticado
            if (!pet.getUserId().equals(user.getId())) {
                logger.warning("Intento de acceso a gastos de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para ver los gastos de esta mascota"));
                return future;
            }

            // Obtener todos los gastos de la mascota
            expenseRepository.findAllByPetId(petId)
                    .thenApply(expenses -> {
                        List<PetExpenseResponse> responses = new ArrayList<>();

                        for (PetExpense expense : expenses) {
                            PetExpenseResponse response = convertToResponse(expense, pet.getName());
                            responses.add(response);
                        }

                        logger.info("Se encontraron " + responses.size() + " gastos para la mascota: " + petId);
                        future.complete(responses);
                        return responses;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al buscar gastos", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar gastos", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<List<PetExpenseResponse>> getExpensesByPetAndCategory(Long petId, ExpenseCategory category,
            String userEmail) {
        logger.info("Buscando gastos por categoría " + category + " para mascota: " + petId);
        CompletableFuture<List<PetExpenseResponse>> future = new CompletableFuture<>();

        try {
            // Verificar que la mascota existe y pertenece al usuario
            logger.info("Buscando usuario: " + userEmail);
            Optional<User> userOpt = userRepository.findUserByEmail(userEmail);

            if (userOpt.isEmpty()) {
                logger.warning("No se encontró el usuario con email: " + userEmail);
                future.completeExceptionally(new IllegalArgumentException("Usuario no encontrado"));
                return future;
            }

            User user = userOpt.get();
            logger.info("Usuario encontrado con ID: " + user.getId());

            // Buscar la mascota por su ID numérico interno
            logger.info("Buscando mascota con ID numérico interno: " + petId);
            Optional<Pet> petOpt = petRepository.findPetByNumericId(petId);

            if (petOpt.isEmpty()) {
                logger.warning("No se encontró la mascota con ID numérico: " + petId);
                future.completeExceptionally(new IllegalArgumentException("Mascota no encontrada"));
                return future;
            }

            Pet pet = petOpt.get();
            logger.info("Mascota encontrada: " + pet.getName());

            // Verificar que la mascota pertenece al usuario autenticado
            if (!pet.getUserId().equals(user.getId())) {
                logger.warning("Intento de acceso a gastos de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para ver los gastos de esta mascota"));
                return future;
            }

            // Obtener los gastos de la mascota filtrados por categoría
            expenseRepository.findAllByPetIdAndCategory(petId, category)
                    .thenApply(expenses -> {
                        List<PetExpenseResponse> responses = new ArrayList<>();

                        for (PetExpense expense : expenses) {
                            PetExpenseResponse response = convertToResponse(expense, pet.getName());
                            responses.add(response);
                        }

                        logger.info("Se encontraron " + responses.size() + " gastos de categoría " + category
                                + " para la mascota: " + petId);
                        future.complete(responses);
                        return responses;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al buscar gastos por categoría", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar gastos por categoría", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<PetExpenseResponse> getExpense(Long expenseId, Long petId, String userEmail) {
        logger.info("Buscando gasto específico. ID: " + expenseId + ", Mascota: " + petId);
        CompletableFuture<PetExpenseResponse> future = new CompletableFuture<>();

        try {
            // Verificar que la mascota existe y pertenece al usuario
            logger.info("Buscando usuario: " + userEmail);
            Optional<User> userOpt = userRepository.findUserByEmail(userEmail);

            if (userOpt.isEmpty()) {
                logger.warning("No se encontró el usuario con email: " + userEmail);
                future.completeExceptionally(new IllegalArgumentException("Usuario no encontrado"));
                return future;
            }

            User user = userOpt.get();
            logger.info("Usuario encontrado con ID: " + user.getId());

            // Buscar la mascota por su ID numérico interno
            logger.info("Buscando mascota con ID numérico interno: " + petId);
            Optional<Pet> petOpt = petRepository.findPetByNumericId(petId);

            if (petOpt.isEmpty()) {
                logger.warning("No se encontró la mascota con ID numérico: " + petId);
                future.completeExceptionally(new IllegalArgumentException("Mascota no encontrada"));
                return future;
            }

            Pet pet = petOpt.get();
            logger.info("Mascota encontrada: " + pet.getName());

            // Verificar que la mascota pertenece al usuario autenticado
            if (!pet.getUserId().equals(user.getId())) {
                logger.warning("Intento de acceso a gasto de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para ver los gastos de esta mascota"));
                return future;
            }

            // Obtener el gasto
            expenseRepository.findById(expenseId)
                    .thenApply(expenseOpt -> {
                        if (expenseOpt.isEmpty()) {
                            logger.warning("No se encontró el gasto con ID: " + expenseId);
                            future.completeExceptionally(new IllegalArgumentException("Gasto no encontrado"));
                            return null;
                        }

                        PetExpense expense = expenseOpt.get();

                        // Verificar que el gasto pertenece a la mascota indicada
                        if (!expense.getPetId().equals(petId)) {
                            logger.warning("El gasto no pertenece a la mascota indicada. GastoMascota: "
                                    + expense.getPetId() + ", MascotaIndicada: " + petId);
                            future.completeExceptionally(
                                    new IllegalArgumentException("El gasto no pertenece a la mascota indicada"));
                            return null;
                        }

                        // Convertir a respuesta
                        PetExpenseResponse response = convertToResponse(expense, pet.getName());
                        logger.info("Gasto encontrado para mascota: " + pet.getName());
                        future.complete(response);
                        return response;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al buscar gasto", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar gasto", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> deleteExpense(Long expenseId, Long petId, String userEmail) {
        logger.info("Eliminando gasto. ID: " + expenseId + ", Mascota: " + petId);
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            // Verificar que la mascota existe y pertenece al usuario
            logger.info("Buscando usuario: " + userEmail);
            Optional<User> userOpt = userRepository.findUserByEmail(userEmail);

            if (userOpt.isEmpty()) {
                logger.warning("No se encontró el usuario con email: " + userEmail);
                future.completeExceptionally(new IllegalArgumentException("Usuario no encontrado"));
                return future;
            }

            User user = userOpt.get();
            logger.info("Usuario encontrado con ID: " + user.getId());

            logger.info("Buscando mascota con ID numérico interno: " + petId);
            Optional<Pet> petOpt = petRepository.findPetByNumericId(petId);

            if (petOpt.isEmpty()) {
                logger.warning("No se encontró la mascota con ID numérico: " + petId);
                future.completeExceptionally(new IllegalArgumentException("Mascota no encontrada"));
                return future;
            }

            Pet pet = petOpt.get();
            logger.info("Mascota encontrada: " + pet.getName());

            // Verificar que la mascota pertenece al usuario autenticado
            if (!pet.getUserId().equals(user.getId())) {
                logger.warning("Intento de eliminar gasto de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para eliminar gastos de esta mascota"));
                return future;
            }

            // Verificar que el gasto existe y pertenece a la mascota
            expenseRepository.existsByIdAndPetId(expenseId, petId)
                    .thenCompose(exists -> {
                        if (!exists) {
                            logger.warning("El gasto no existe o no pertenece a la mascota indicada");
                            future.completeExceptionally(new IllegalArgumentException(
                                    "El gasto no existe o no pertenece a la mascota indicada"));
                            return CompletableFuture.completedFuture(false);
                        }

                        // Buscar primero el gasto para obtener la URL de la imagen de recibo (si
                        // existe)
                        return expenseRepository.findById(expenseId)
                                .thenCompose(expenseOpt -> {
                                    if (expenseOpt.isPresent() && expenseOpt.get().getReceiptImageUrl() != null
                                            && !expenseOpt.get().getReceiptImageUrl().isEmpty()) {
                                        // Eliminar la imagen si existe
                                        try {
                                            fileStorageService.deleteFile(expenseOpt.get().getReceiptImageUrl());
                                            logger.info("Imagen de recibo eliminada: "
                                                    + expenseOpt.get().getReceiptImageUrl());
                                        } catch (Exception e) {
                                            logger.warning(
                                                    "No se pudo eliminar la imagen de recibo: " + e.getMessage());
                                        }
                                    }

                                    // Continuar con la eliminación del gasto
                                    return expenseRepository.deleteById(expenseId)
                                            .thenApply(v -> {
                                                logger.info("Gasto eliminado con éxito. ID: " + expenseId);
                                                future.complete(true);
                                                return true;
                                            });
                                });
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al eliminar gasto", ex);
                        future.completeExceptionally(ex);
                        return false;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al eliminar gasto", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<String> uploadReceiptImage(Long expenseId, Long petId, String userEmail,
            MultipartFile receiptImage) throws IOException {
        logger.info("Subiendo imagen de recibo para gasto ID: " + expenseId + ", Mascota: " + petId);
        CompletableFuture<String> future = new CompletableFuture<>();

        // Validar archivo
        if (receiptImage == null || receiptImage.isEmpty()) {
            logger.warning("Archivo de imagen es nulo o vacío");
            future.completeExceptionally(new IllegalArgumentException("La imagen no puede estar vacía"));
            return future;
        }

        // Validar que el archivo sea una imagen
        String contentType = receiptImage.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            logger.warning("El archivo no es una imagen válida. Content-Type: " + contentType);
            future.completeExceptionally(new IllegalArgumentException("El archivo debe ser una imagen"));
            return future;
        }

        try {
            // Verificar que la mascota existe y pertenece al usuario
            logger.info("Buscando usuario: " + userEmail);
            Optional<User> userOpt = userRepository.findUserByEmail(userEmail);

            if (userOpt.isEmpty()) {
                logger.warning("No se encontró el usuario con email: " + userEmail);
                future.completeExceptionally(new IllegalArgumentException("Usuario no encontrado"));
                return future;
            }

            User user = userOpt.get();
            logger.info("Usuario encontrado con ID: " + user.getId());

            logger.info("Buscando mascota con ID numérico interno: " + petId);
            Optional<Pet> petOpt = petRepository.findPetByNumericId(petId);

            if (petOpt.isEmpty()) {
                logger.warning("No se encontró la mascota con ID numérico: " + petId);
                future.completeExceptionally(new IllegalArgumentException("Mascota no encontrada"));
                return future;
            }

            Pet pet = petOpt.get();
            logger.info("Mascota encontrada: " + pet.getName());

            // Verificar que la mascota pertenece al usuario autenticado
            if (!pet.getUserId().equals(user.getId())) {
                logger.warning("Intento de actualizar imagen de gasto de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para modificar gastos de esta mascota"));
                return future;
            }

            // Verificar que el gasto existe y pertenece a la mascota
            expenseRepository.existsByIdAndPetId(expenseId, petId)
                    .thenCompose(exists -> {
                        if (!exists) {
                            logger.warning("El gasto no existe o no pertenece a la mascota indicada");
                            future.completeExceptionally(new IllegalArgumentException(
                                    "El gasto no existe o no pertenece a la mascota indicada"));
                            return CompletableFuture.completedFuture(null);
                        }

                        // Primero verificar si ya existe una imagen de recibo
                        return expenseRepository.findById(expenseId)
                                .thenCompose(expenseOpt -> {
                                    PetExpense expense = expenseOpt.get(); // Ya sabemos que existe

                                    // Si ya hay una imagen, eliminarla primero
                                    if (expense.getReceiptImageUrl() != null
                                            && !expense.getReceiptImageUrl().isEmpty()) {
                                        try {
                                            fileStorageService.deleteFile(expense.getReceiptImageUrl());
                                            logger.info("Imagen de recibo anterior eliminada: "
                                                    + expense.getReceiptImageUrl());
                                        } catch (Exception e) {
                                            logger.warning("No se pudo eliminar la imagen de recibo anterior: "
                                                    + e.getMessage());
                                            // Continuamos de todos modos, ya que esto no debería impedir subir la nueva
                                            // imagen
                                        }
                                    }

                                    try {
                                        // Guardar la nueva imagen en el directorio "receipts"
                                        String imageUrl = fileStorageService.storeFile(receiptImage, "receipts");
                                        logger.info("Imagen de recibo guardada: " + imageUrl);

                                        // Actualizar la URL de la imagen en el gasto
                                        return expenseRepository.updateReceiptImage(expenseId, imageUrl)
                                                .thenApply(updatedExpense -> {
                                                    future.complete(imageUrl);
                                                    return imageUrl;
                                                });
                                    } catch (IOException e) {
                                        logger.log(Level.SEVERE, "Error al guardar la imagen", e);
                                        future.completeExceptionally(e);
                                        return CompletableFuture.completedFuture(null);
                                    }
                                });
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al subir imagen de recibo", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al subir imagen de recibo", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    // Método auxiliar para convertir un gasto a su respuesta
    private PetExpenseResponse convertToResponse(PetExpense expense, String petName) {
        PetExpenseResponse response = new PetExpenseResponse();
        response.setId(expense.getId());
        response.setPetId(expense.getPetId());
        response.setPetName(petName);

        response.setTitle(expense.getTitle());
        response.setDescription(expense.getDescription());
        response.setAmount(expense.getAmount());
        response.setCurrencyCode(expense.getCurrencyCode());
        response.setCategory(expense.getCategory());
        response.setDate(expense.getDateStr());
        response.setVendorName(expense.getVendorName());
        response.setReceiptImageUrl(expense.getReceiptImageUrl());
        response.setCreatedAt(expense.getCreatedAtStr());

        return response;
    }
}