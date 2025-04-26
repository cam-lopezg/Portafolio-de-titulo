package cl.pets.patitaspet.pet.service;

import cl.pets.patitaspet.pet.dto.PetVaccinationRequest;
import cl.pets.patitaspet.pet.dto.PetVaccinationResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para gestionar el historial de vacunaciones de las mascotas
 */
public interface PetVaccinationService {

    /**
     * Registra una nueva vacunación para una mascota
     * 
     * @param petId     ID de la mascota
     * @param request   Datos de la vacunación
     * @param userEmail Email del usuario autenticado (para validación)
     * @return La vacunación creada
     * @throws IllegalArgumentException Si los datos son inválidos o el usuario no
     *                                  es dueño de la mascota
     */
    CompletableFuture<PetVaccinationResponse> createVaccination(Long petId, PetVaccinationRequest request,
            String userEmail);

    /**
     * Obtiene todas las vacunaciones de una mascota
     * 
     * @param petId     ID de la mascota
     * @param userEmail Email del usuario autenticado (para validación)
     * @return Lista de vacunaciones
     * @throws IllegalArgumentException Si la mascota no existe o no pertenece al
     *                                  usuario
     */
    CompletableFuture<List<PetVaccinationResponse>> getVaccinationsByPet(Long petId, String userEmail);

    /**
     * Obtiene una vacunación específica
     * 
     * @param vaccinationId ID de la vacunación
     * @param petId         ID de la mascota
     * @param userEmail     Email del usuario autenticado (para validación)
     * @return La vacunación solicitada
     * @throws IllegalArgumentException Si la vacunación no existe o no pertenece a
     *                                  la mascota indicada
     */
    CompletableFuture<PetVaccinationResponse> getVaccination(Long vaccinationId, Long petId, String userEmail);

    /**
     * Elimina una vacunación
     * 
     * @param vaccinationId ID de la vacunación
     * @param petId         ID de la mascota
     * @param userEmail     Email del usuario autenticado (para validación)
     * @return true si se eliminó correctamente
     * @throws IllegalArgumentException Si la vacunación no existe o no pertenece a
     *                                  la mascota indicada
     */
    CompletableFuture<Boolean> deleteVaccination(Long vaccinationId, Long petId, String userEmail);
}