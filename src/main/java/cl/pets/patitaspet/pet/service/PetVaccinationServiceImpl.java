package cl.pets.patitaspet.pet.service;

import cl.pets.patitaspet.pet.dto.PetVaccinationRequest;
import cl.pets.patitaspet.pet.dto.PetVaccinationResponse;
import cl.pets.patitaspet.pet.entity.Pet;
import cl.pets.patitaspet.pet.entity.PetVaccination;
import cl.pets.patitaspet.pet.repository.FirestorePetRepository;
import cl.pets.patitaspet.pet.repository.PetVaccinationRepository;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.repository.FirestoreUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PetVaccinationServiceImpl implements PetVaccinationService {

    private static final Logger logger = Logger.getLogger(PetVaccinationServiceImpl.class.getName());

    @Autowired
    private PetVaccinationRepository vaccinationRepository;

    @Autowired
    private FirestorePetRepository petRepository;

    @Autowired
    private FirestoreUserRepository userRepository;

    @Override
    public CompletableFuture<PetVaccinationResponse> createVaccination(Long petId, PetVaccinationRequest request,
            String userEmail) {
        logger.info("Iniciando creación de registro de vacunación para mascota: " + petId);

        // CompletableFuture para el resultado
        CompletableFuture<PetVaccinationResponse> future = new CompletableFuture<>();

        // Validar datos
        if (request == null) {
            logger.warning("Request de vacunación es nulo");
            future.completeExceptionally(new IllegalArgumentException("La solicitud no puede ser nula"));
            return future;
        }

        if (petId == null) {
            logger.warning("ID de mascota es nulo");
            future.completeExceptionally(new IllegalArgumentException("El ID de la mascota no puede ser nulo"));
            return future;
        }

        // Validar nombre de vacuna
        if (request.getVaccineName() == null || request.getVaccineName().trim().isEmpty()) {
            logger.warning("Nombre de vacuna vacío");
            future.completeExceptionally(new IllegalArgumentException("El nombre de la vacuna no puede estar vacío"));
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
                logger.warning("Intento de registro de vacuna para mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para registrar vacunaciones a esta mascota"));
                return future;
            }

            // Crear la entidad de vacunación
            PetVaccination vaccination = new PetVaccination();
            vaccination.setPetId(petId);

            // Transferir datos del request
            vaccination.setVaccineName(request.getVaccineName());
            vaccination.setDescription(request.getDescription());
            vaccination.setDateGivenStr(request.getDateGiven());
            vaccination.setNextDueDateStr(request.getNextDueDate());

            vaccination.setMultiDose(request.getMultiDose() != null && request.getMultiDose());
            vaccination.setDoseNumber(request.getDoseNumber());
            vaccination.setTotalDoses(request.getTotalDoses());

            vaccination.setNotes(request.getNotes());
            vaccination.setVeterinarianName(request.getVeterinarianName());

            // Guardar la vacunación
            logger.info("Guardando registro de vacunación para mascota: " + petId);
            vaccinationRepository.save(vaccination)
                    .thenApply(savedVaccination -> {
                        // Convertir a respuesta
                        PetVaccinationResponse response = new PetVaccinationResponse();
                        response.setId(savedVaccination.getId());
                        response.setPetId(savedVaccination.getPetId());
                        response.setPetName(pet.getName());

                        response.setVaccineName(savedVaccination.getVaccineName());
                        response.setDescription(savedVaccination.getDescription());
                        response.setDateGivenStr(savedVaccination.getDateGivenStr());
                        response.setNextDueDateStr(savedVaccination.getNextDueDateStr());

                        response.setMultiDose(savedVaccination.isMultiDose());
                        response.setDoseNumber(savedVaccination.getDoseNumber());
                        response.setTotalDoses(savedVaccination.getTotalDoses());

                        response.setNotes(savedVaccination.getNotes());
                        response.setVeterinarianName(savedVaccination.getVeterinarianName());
                        response.setCreatedAtStr(savedVaccination.getCreatedAtStr());

                        logger.info("Vacunación registrada con éxito. ID: " + savedVaccination.getId());
                        future.complete(response);
                        return response;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al guardar vacunación", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al registrar vacunación", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<List<PetVaccinationResponse>> getVaccinationsByPet(Long petId, String userEmail) {
        logger.info("Buscando vacunaciones para mascota: " + petId);
        CompletableFuture<List<PetVaccinationResponse>> future = new CompletableFuture<>();

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
                logger.warning("Intento de acceso a vacunaciones de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para ver las vacunaciones de esta mascota"));
                return future;
            }

            // Obtener todas las vacunaciones de la mascota
            vaccinationRepository.findAllByPetId(petId)
                    .thenApply(vaccinations -> {
                        List<PetVaccinationResponse> responses = new ArrayList<>();

                        for (PetVaccination vaccination : vaccinations) {
                            PetVaccinationResponse response = new PetVaccinationResponse();
                            response.setId(vaccination.getId());
                            response.setPetId(vaccination.getPetId());
                            response.setPetName(pet.getName());

                            response.setVaccineName(vaccination.getVaccineName());
                            response.setDescription(vaccination.getDescription());
                            response.setDateGivenStr(vaccination.getDateGivenStr());
                            response.setNextDueDateStr(vaccination.getNextDueDateStr());

                            response.setMultiDose(vaccination.isMultiDose());
                            response.setDoseNumber(vaccination.getDoseNumber());
                            response.setTotalDoses(vaccination.getTotalDoses());

                            response.setNotes(vaccination.getNotes());
                            response.setVeterinarianName(vaccination.getVeterinarianName());
                            response.setCreatedAtStr(vaccination.getCreatedAtStr());

                            responses.add(response);
                        }

                        logger.info("Se encontraron " + responses.size() + " vacunaciones para la mascota: " + petId);
                        future.complete(responses);
                        return responses;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al buscar vacunaciones", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar vacunaciones", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<PetVaccinationResponse> getVaccination(Long vaccinationId, Long petId, String userEmail) {
        logger.info("Buscando vacunación específica. ID: " + vaccinationId + ", Mascota: " + petId);
        CompletableFuture<PetVaccinationResponse> future = new CompletableFuture<>();

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
                logger.warning("Intento de acceso a vacunación de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para ver las vacunaciones de esta mascota"));
                return future;
            }

            // Obtener la vacunación
            vaccinationRepository.findById(vaccinationId)
                    .thenApply(vaccinationOpt -> {
                        if (vaccinationOpt.isEmpty()) {
                            logger.warning("No se encontró la vacunación con ID: " + vaccinationId);
                            future.completeExceptionally(new IllegalArgumentException("Vacunación no encontrada"));
                            return null;
                        }

                        PetVaccination vaccination = vaccinationOpt.get();

                        // Verificar que la vacunación pertenece a la mascota indicada
                        if (!vaccination.getPetId().equals(petId)) {
                            logger.warning("La vacunación no pertenece a la mascota indicada. VacunaciónMascota: "
                                    + vaccination.getPetId() + ", MascotaIndicada: " + petId);
                            future.completeExceptionally(
                                    new IllegalArgumentException("La vacunación no pertenece a la mascota indicada"));
                            return null;
                        }

                        // Convertir a respuesta
                        PetVaccinationResponse response = new PetVaccinationResponse();
                        response.setId(vaccination.getId());
                        response.setPetId(vaccination.getPetId());
                        response.setPetName(pet.getName());

                        response.setVaccineName(vaccination.getVaccineName());
                        response.setDescription(vaccination.getDescription());
                        response.setDateGivenStr(vaccination.getDateGivenStr());
                        response.setNextDueDateStr(vaccination.getNextDueDateStr());

                        response.setMultiDose(vaccination.isMultiDose());
                        response.setDoseNumber(vaccination.getDoseNumber());
                        response.setTotalDoses(vaccination.getTotalDoses());

                        response.setNotes(vaccination.getNotes());
                        response.setVeterinarianName(vaccination.getVeterinarianName());
                        response.setCreatedAtStr(vaccination.getCreatedAtStr());

                        logger.info("Vacunación encontrada para mascota: " + pet.getName());
                        future.complete(response);
                        return response;
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al buscar vacunación", ex);
                        future.completeExceptionally(ex);
                        return null;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar vacunación", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> deleteVaccination(Long vaccinationId, Long petId, String userEmail) {
        logger.info("Eliminando vacunación. ID: " + vaccinationId + ", Mascota: " + petId);
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
                logger.warning("Intento de eliminar vacunación de mascota que no pertenece al usuario. Usuario: "
                        + user.getId() + ", MascotaUsuario: " + pet.getUserId());
                future.completeExceptionally(
                        new AccessDeniedException("No tienes permiso para eliminar vacunaciones de esta mascota"));
                return future;
            }

            // Verificar que la vacunación existe y pertenece a la mascota
            vaccinationRepository.existsByIdAndPetId(vaccinationId, petId)
                    .thenCompose(exists -> {
                        if (!exists) {
                            logger.warning("La vacunación no existe o no pertenece a la mascota indicada");
                            future.completeExceptionally(new IllegalArgumentException(
                                    "La vacunación no existe o no pertenece a la mascota indicada"));
                            return CompletableFuture.completedFuture(false);
                        }

                        // Eliminar la vacunación
                        return vaccinationRepository.deleteById(vaccinationId)
                                .thenApply(v -> {
                                    logger.info("Vacunación eliminada con éxito. ID: " + vaccinationId);
                                    future.complete(true);
                                    return true;
                                });
                    })
                    .exceptionally(ex -> {
                        logger.log(Level.SEVERE, "Error al eliminar vacunación", ex);
                        future.completeExceptionally(ex);
                        return false;
                    });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al eliminar vacunación", e);
            future.completeExceptionally(e);
        }

        return future;
    }
}