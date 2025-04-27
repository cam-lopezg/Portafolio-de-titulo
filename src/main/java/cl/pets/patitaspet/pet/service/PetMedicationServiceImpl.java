package cl.pets.patitaspet.pet.service;

import cl.pets.patitaspet.pet.dto.PetMedicationRequest;
import cl.pets.patitaspet.pet.dto.PetMedicationResponse;
import cl.pets.patitaspet.pet.entity.Pet;
import cl.pets.patitaspet.pet.entity.PetMedication;
import cl.pets.patitaspet.pet.repository.FirestorePetRepository;
import cl.pets.patitaspet.pet.repository.PetMedicationRepository;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.repository.FirestoreUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PetMedicationServiceImpl implements PetMedicationService {

    private static final Logger logger = Logger.getLogger(PetMedicationServiceImpl.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private PetMedicationRepository medicationRepository;

    @Autowired
    private FirestorePetRepository petRepository;

    @Autowired
    private FirestoreUserRepository userRepository;

    @Override
    public CompletableFuture<PetMedicationResponse> createMedication(Long petId, PetMedicationRequest request,
            String userEmail) {
        logger.info("Iniciando creación de medicamento para mascota: " + petId);

        // CompletableFuture para el resultado
        CompletableFuture<PetMedicationResponse> future = new CompletableFuture<>();

        // Validar datos
        if (request == null) {
            logger.warning("Request de medicamento es nulo");
            future.completeExceptionally(new IllegalArgumentException("La solicitud no puede ser nula"));
            return future;
        }

        if (petId == null) {
            logger.warning("ID de mascota es nulo");
            future.completeExceptionally(new IllegalArgumentException("El ID de la mascota no puede ser nulo"));
            return future;
        }

        // Validar nombre del medicamento
        if (request.getMedicationName() == null || request.getMedicationName().trim().isEmpty()) {
            logger.warning("Nombre de medicamento vacío");
            future.completeExceptionally(
                    new IllegalArgumentException("El nombre del medicamento no puede estar vacío"));
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
                logger.warning("Intento de registro de medicamento para mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para registrar medicamentos a esta mascota"));
                return future;
            }

            // Crear la entidad de medicamento
            PetMedication medication = new PetMedication();
            medication.setPet(pet);

            // Transferir datos del request
            medication.setMedicationName(request.getMedicationName());

            // Convertir fechas de String a LocalDate
            if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
                try {
                    medication.setStartDate(LocalDate.parse(request.getStartDate()));
                } catch (Exception e) {
                    future.completeExceptionally(
                            new IllegalArgumentException("Formato de fecha de inicio inválido. Use YYYY-MM-DD"));
                    return future;
                }
            }

            if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
                try {
                    medication.setEndDate(LocalDate.parse(request.getEndDate()));
                } catch (Exception e) {
                    future.completeExceptionally(
                            new IllegalArgumentException("Formato de fecha de finalización inválido. Use YYYY-MM-DD"));
                    return future;
                }
            }

            medication.setDosageInstructions(request.getDosageInstructions());
            medication.setNotes(request.getNotes());

            // Guardar el medicamento
            logger.info("Guardando registro de medicamento para mascota: " + petId);

            medicationRepository.saveMedication(medication)
                    .thenApply(savedId -> {
                        // Convertir a respuesta
                        PetMedicationResponse response = new PetMedicationResponse();
                        response.setId(medication.getId());
                        response.setPetId(pet.getId());
                        response.setMedicationName(medication.getMedicationName());

                        // Convertir fechas a formato String
                        if (medication.getStartDate() != null) {
                            response.setStartDate(medication.getStartDate().format(DATE_FORMATTER));
                        }

                        if (medication.getEndDate() != null) {
                            response.setEndDate(medication.getEndDate().format(DATE_FORMATTER));
                        }

                        response.setDosageInstructions(medication.getDosageInstructions());
                        response.setNotes(medication.getNotes());

                        logger.info("Medicamento registrado con éxito. ID: " + medication.getId());
                        future.complete(response);
                        return response;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al guardar medicamento", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al registrar medicamento", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<List<PetMedicationResponse>> getMedicationsByPet(Long petId, String userEmail) {
        logger.info("Buscando medicamentos para mascota: " + petId);
        CompletableFuture<List<PetMedicationResponse>> future = new CompletableFuture<>();

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
                logger.warning("Intento de acceso a medicamentos de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para ver los medicamentos de esta mascota"));
                return future;
            }

            // Obtener todos los medicamentos de la mascota
            medicationRepository.findMedicationsByPetId(petId)
                    .thenApply(medications -> {
                        List<PetMedicationResponse> responses = new ArrayList<>();

                        for (PetMedication medication : medications) {
                            PetMedicationResponse response = new PetMedicationResponse();
                            response.setId(medication.getId());
                            response.setPetId(pet.getId());
                            response.setMedicationName(medication.getMedicationName());

                            // Convertir fechas a formato String
                            if (medication.getStartDate() != null) {
                                response.setStartDate(medication.getStartDate().format(DATE_FORMATTER));
                            }

                            if (medication.getEndDate() != null) {
                                response.setEndDate(medication.getEndDate().format(DATE_FORMATTER));
                            }

                            response.setDosageInstructions(medication.getDosageInstructions());
                            response.setNotes(medication.getNotes());

                            responses.add(response);
                        }

                        logger.info("Se encontraron " + responses.size() + " medicamentos para la mascota: " + petId);
                        future.complete(responses);
                        return responses;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al buscar medicamentos", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar medicamentos", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<PetMedicationResponse> getMedication(Long medicationId, Long petId, String userEmail) {
        logger.info("Buscando medicamento específico. ID: " + medicationId + ", Mascota: " + petId);
        CompletableFuture<PetMedicationResponse> future = new CompletableFuture<>();

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
                logger.warning("Intento de acceso a medicamento de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para ver los medicamentos de esta mascota"));
                return future;
            }

            // Obtener el medicamento
            medicationRepository.findMedicationById(medicationId)
                    .thenApply(medicationOpt -> {
                        if (medicationOpt.isEmpty()) {
                            logger.warning("No se encontró el medicamento con ID: " + medicationId);
                            future.completeExceptionally(new IllegalArgumentException("Medicamento no encontrado"));
                            return null;
                        }

                        PetMedication medication = medicationOpt.get();

                        // Verificar que el medicamento pertenece a la mascota indicada
                        if (medication.getPet() == null || !medication.getPet().getId().equals(petId)) {
                            logger.warning("El medicamento no pertenece a la mascota indicada. MedicamentoMascota: "
                                    + (medication.getPet() != null ? medication.getPet().getId() : "null")
                                    + ", MascotaIndicada: " + petId);
                            future.completeExceptionally(
                                    new IllegalArgumentException("El medicamento no pertenece a la mascota indicada"));
                            return null;
                        }

                        // Convertir a respuesta
                        PetMedicationResponse response = new PetMedicationResponse();
                        response.setId(medication.getId());
                        response.setPetId(pet.getId());
                        response.setMedicationName(medication.getMedicationName());

                        // Convertir fechas a formato String
                        if (medication.getStartDate() != null) {
                            response.setStartDate(medication.getStartDate().format(DATE_FORMATTER));
                        }

                        if (medication.getEndDate() != null) {
                            response.setEndDate(medication.getEndDate().format(DATE_FORMATTER));
                        }

                        response.setDosageInstructions(medication.getDosageInstructions());
                        response.setNotes(medication.getNotes());

                        logger.info("Medicamento encontrado para mascota: " + pet.getName());
                        future.complete(response);
                        return response;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al buscar medicamento", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar medicamento", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<PetMedicationResponse> updateMedication(Long medicationId, Long petId,
            PetMedicationRequest request, String userEmail) {
        logger.info("Actualizando medicamento. ID: " + medicationId + ", Mascota: " + petId);
        CompletableFuture<PetMedicationResponse> future = new CompletableFuture<>();

        // Validar datos
        if (request == null) {
            logger.warning("Request de actualización de medicamento es nulo");
            future.completeExceptionally(new IllegalArgumentException("La solicitud no puede ser nula"));
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
                logger.warning("Intento de actualizar medicamento de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para actualizar medicamentos de esta mascota"));
                return future;
            }

            // Verificar que el medicamento existe
            medicationRepository.findMedicationById(medicationId)
                    .thenCompose(medicationOpt -> {
                        if (medicationOpt.isEmpty()) {
                            logger.warning("No se encontró el medicamento con ID: " + medicationId);
                            future.completeExceptionally(new IllegalArgumentException("Medicamento no encontrado"));
                            return CompletableFuture.completedFuture(null);
                        }

                        PetMedication medication = medicationOpt.get();

                        // Verificar que el medicamento pertenece a la mascota indicada
                        if (medication.getPet() == null || !medication.getPet().getId().equals(petId)) {
                            logger.warning("El medicamento no pertenece a la mascota indicada. MedicamentoMascota: "
                                    + (medication.getPet() != null ? medication.getPet().getId() : "null")
                                    + ", MascotaIndicada: " + petId);
                            future.completeExceptionally(
                                    new IllegalArgumentException("El medicamento no pertenece a la mascota indicada"));
                            return CompletableFuture.completedFuture(null);
                        }

                        // Actualizar los datos del medicamento
                        if (request.getMedicationName() != null && !request.getMedicationName().isEmpty()) {
                            medication.setMedicationName(request.getMedicationName());
                        }

                        // Actualizar fechas si se proporcionan
                        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
                            try {
                                medication.setStartDate(LocalDate.parse(request.getStartDate()));
                            } catch (Exception e) {
                                future.completeExceptionally(new IllegalArgumentException(
                                        "Formato de fecha de inicio inválido. Use YYYY-MM-DD"));
                                return CompletableFuture.completedFuture(null);
                            }
                        }

                        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
                            try {
                                medication.setEndDate(LocalDate.parse(request.getEndDate()));
                            } catch (Exception e) {
                                future.completeExceptionally(new IllegalArgumentException(
                                        "Formato de fecha de finalización inválido. Use YYYY-MM-DD"));
                                return CompletableFuture.completedFuture(null);
                            }
                        }

                        if (request.getDosageInstructions() != null) {
                            medication.setDosageInstructions(request.getDosageInstructions());
                        }

                        if (request.getNotes() != null) {
                            medication.setNotes(request.getNotes());
                        }

                        // Guardar los cambios
                        return medicationRepository.updateMedication(medication)
                                .thenApply(updated -> {
                                    if (!updated) {
                                        logger.warning("Error al actualizar el medicamento");
                                        future.completeExceptionally(
                                                new RuntimeException("Error al actualizar el medicamento"));
                                        return null;
                                    }

                                    // Convertir a respuesta
                                    PetMedicationResponse response = new PetMedicationResponse();
                                    response.setId(medication.getId());
                                    response.setPetId(pet.getId());
                                    response.setMedicationName(medication.getMedicationName());

                                    // Convertir fechas a formato String
                                    if (medication.getStartDate() != null) {
                                        response.setStartDate(medication.getStartDate().format(DATE_FORMATTER));
                                    }

                                    if (medication.getEndDate() != null) {
                                        response.setEndDate(medication.getEndDate().format(DATE_FORMATTER));
                                    }

                                    response.setDosageInstructions(medication.getDosageInstructions());
                                    response.setNotes(medication.getNotes());

                                    logger.info("Medicamento actualizado con éxito. ID: " + medication.getId());
                                    future.complete(response);
                                    return response;
                                });
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al actualizar medicamento", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al actualizar medicamento", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> deleteMedication(Long medicationId, Long petId, String userEmail) {
        logger.info("Eliminando medicamento. ID: " + medicationId + ", Mascota: " + petId);
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
                logger.warning("Intento de eliminar medicamento de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para eliminar medicamentos de esta mascota"));
                return future;
            }

            // Verificar que el medicamento existe y pertenece a la mascota
            medicationRepository.existsByIdAndPetId(medicationId, petId)
                    .thenCompose(exists -> {
                        if (!exists) {
                            logger.warning("El medicamento no existe o no pertenece a la mascota indicada");
                            future.completeExceptionally(new IllegalArgumentException(
                                    "El medicamento no existe o no pertenece a la mascota indicada"));
                            return CompletableFuture.completedFuture(false);
                        }

                        // Eliminar el medicamento
                        return medicationRepository.deleteMedication(medicationId)
                                .thenApply(v -> {
                                    logger.info("Medicamento eliminado con éxito. ID: " + medicationId);
                                    future.complete(true);
                                    return true;
                                });
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al eliminar medicamento", ex);
                        future.completeExceptionally(ex);
                        return false;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al eliminar medicamento", e);
            future.completeExceptionally(e);
        }

        return future;
    }
}