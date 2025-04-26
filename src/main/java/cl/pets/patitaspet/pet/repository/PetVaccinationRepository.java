package cl.pets.patitaspet.pet.repository;

import cl.pets.patitaspet.pet.entity.PetVaccination;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio para gestionar el historial de vacunaciones de las mascotas
 */
public interface PetVaccinationRepository {
    /**
     * Crea un nuevo registro de vacunación
     */
    CompletableFuture<PetVaccination> save(PetVaccination vaccination);

    /**
     * Obtiene todas las vacunaciones de una mascota específica
     */
    CompletableFuture<List<PetVaccination>> findAllByPetId(Long petId);

    /**
     * Obtiene un registro de vacunación por su ID
     */
    CompletableFuture<Optional<PetVaccination>> findById(Long id);

    /**
     * Elimina un registro de vacunación
     */
    CompletableFuture<Void> deleteById(Long id);

    /**
     * Verifica si un registro de vacunación existe y pertenece a una mascota
     * específica
     */
    CompletableFuture<Boolean> existsByIdAndPetId(Long id, Long petId);
}