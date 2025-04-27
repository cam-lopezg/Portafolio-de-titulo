package cl.pets.patitaspet.pet.repository;

import cl.pets.patitaspet.pet.entity.PetMedication;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio para gestionar los medicamentos de las mascotas
 */
public interface PetMedicationRepository {

    /**
     * Guarda un nuevo medicamento
     */
    CompletableFuture<Long> saveMedication(PetMedication medication);

    /**
     * Obtiene todos los medicamentos de una mascota específica
     */
    CompletableFuture<List<PetMedication>> findMedicationsByPetId(Long petId);

    /**
     * Obtiene un medicamento por su ID
     */
    CompletableFuture<Optional<PetMedication>> findMedicationById(Long medicationId);

    /**
     * Actualiza un medicamento existente
     */
    CompletableFuture<Boolean> updateMedication(PetMedication medication);

    /**
     * Elimina un medicamento
     */
    CompletableFuture<Void> deleteMedication(Long medicationId);

    /**
     * Verifica si un medicamento existe y pertenece a una mascota específica
     */
    CompletableFuture<Boolean> existsByIdAndPetId(Long medicationId, Long petId);
}