package cl.pets.patitaspet.pet.service;

import cl.pets.patitaspet.pet.dto.PetMedicationRequest;
import cl.pets.patitaspet.pet.dto.PetMedicationResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para gestionar los medicamentos de las mascotas
 */
public interface PetMedicationService {

    /**
     * Registra un nuevo medicamento para una mascota
     * 
     * @param petId     ID de la mascota
     * @param request   Datos del medicamento
     * @param userEmail Email del usuario autenticado (para validación)
     * @return El medicamento creado
     * @throws IllegalArgumentException Si los datos son inválidos o el usuario no
     *                                  es dueño de la mascota
     */
    CompletableFuture<PetMedicationResponse> createMedication(Long petId, PetMedicationRequest request,
            String userEmail);

    /**
     * Obtiene todos los medicamentos de una mascota
     * 
     * @param petId     ID de la mascota
     * @param userEmail Email del usuario autenticado (para validación)
     * @return Lista de medicamentos
     * @throws IllegalArgumentException Si la mascota no existe
     * @throws AccessDeniedException    Si el usuario no es dueño de la mascota
     */
    CompletableFuture<List<PetMedicationResponse>> getMedicationsByPet(Long petId, String userEmail);

    /**
     * Obtiene un medicamento específico
     * 
     * @param medicationId ID del medicamento
     * @param petId        ID de la mascota
     * @param userEmail    Email del usuario autenticado (para validación)
     * @return El medicamento solicitado
     * @throws IllegalArgumentException Si el medicamento no existe o no pertenece a
     *                                  la mascota
     * @throws AccessDeniedException    Si el usuario no es dueño de la mascota
     */
    CompletableFuture<PetMedicationResponse> getMedication(Long medicationId, Long petId, String userEmail);

    /**
     * Actualiza un medicamento existente
     * 
     * @param medicationId ID del medicamento
     * @param petId        ID de la mascota
     * @param request      Datos actualizados del medicamento
     * @param userEmail    Email del usuario autenticado (para validación)
     * @return El medicamento actualizado
     * @throws IllegalArgumentException Si el medicamento no existe o no pertenece a
     *                                  la mascota
     * @throws AccessDeniedException    Si el usuario no es dueño de la mascota
     */
    CompletableFuture<PetMedicationResponse> updateMedication(Long medicationId, Long petId,
            PetMedicationRequest request, String userEmail);

    /**
     * Elimina un medicamento
     * 
     * @param medicationId ID del medicamento
     * @param petId        ID de la mascota
     * @param userEmail    Email del usuario autenticado (para validación)
     * @return true si se eliminó correctamente
     * @throws IllegalArgumentException Si el medicamento no existe o no pertenece a
     *                                  la mascota
     * @throws AccessDeniedException    Si el usuario no es dueño de la mascota
     */
    CompletableFuture<Boolean> deleteMedication(Long medicationId, Long petId, String userEmail);
}