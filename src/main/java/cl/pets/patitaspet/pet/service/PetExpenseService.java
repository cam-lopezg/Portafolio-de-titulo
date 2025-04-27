package cl.pets.patitaspet.pet.service;

import cl.pets.patitaspet.pet.dto.PetExpenseRequest;
import cl.pets.patitaspet.pet.dto.PetExpenseResponse;
import cl.pets.patitaspet.pet.entity.ExpenseCategory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para gestionar el historial de gastos de las mascotas
 */
public interface PetExpenseService {

    /**
     * Registra un nuevo gasto para una mascota
     * 
     * @param petId     ID de la mascota
     * @param request   Datos del gasto
     * @param userEmail Email del usuario autenticado (para validación)
     * @return El gasto creado
     * @throws IllegalArgumentException Si los datos son inválidos o el usuario no
     *                                  es dueño de la mascota
     */
    CompletableFuture<PetExpenseResponse> createExpense(Long petId, PetExpenseRequest request,
            String userEmail);

    /**
     * Obtiene todos los gastos de una mascota
     * 
     * @param petId     ID de la mascota
     * @param userEmail Email del usuario autenticado (para validación)
     * @return Lista de gastos
     * @throws IllegalArgumentException Si la mascota no existe o no pertenece al
     *                                  usuario
     */
    CompletableFuture<List<PetExpenseResponse>> getExpensesByPet(Long petId, String userEmail);

    /**
     * Obtiene todos los gastos de una mascota filtrados por categoría
     * 
     * @param petId     ID de la mascota
     * @param category  Categoría de gastos a filtrar
     * @param userEmail Email del usuario autenticado (para validación)
     * @return Lista de gastos filtrados por categoría
     * @throws IllegalArgumentException Si la mascota no existe o no pertenece al
     *                                  usuario
     */
    CompletableFuture<List<PetExpenseResponse>> getExpensesByPetAndCategory(Long petId, ExpenseCategory category,
            String userEmail);

    /**
     * Obtiene un gasto específico
     * 
     * @param expenseId ID del gasto
     * @param petId     ID de la mascota
     * @param userEmail Email del usuario autenticado (para validación)
     * @return El gasto solicitado
     * @throws IllegalArgumentException Si el gasto no existe o no pertenece a
     *                                  la mascota indicada
     */
    CompletableFuture<PetExpenseResponse> getExpense(Long expenseId, Long petId, String userEmail);

    /**
     * Elimina un gasto
     * 
     * @param expenseId ID del gasto
     * @param petId     ID de la mascota
     * @param userEmail Email del usuario autenticado (para validación)
     * @return true si se eliminó correctamente
     * @throws IllegalArgumentException Si el gasto no existe o no pertenece a
     *                                  la mascota indicada
     */
    CompletableFuture<Boolean> deleteExpense(Long expenseId, Long petId, String userEmail);

    /**
     * Sube una imagen de recibo para un gasto
     * 
     * @param expenseId    ID del gasto
     * @param petId        ID de la mascota
     * @param userEmail    Email del usuario autenticado
     * @param receiptImage Archivo de imagen
     * @return La URL de la imagen subida
     * @throws IllegalArgumentException Si el gasto no existe o no pertenece a la
     *                                  mascota indicada
     */
    CompletableFuture<String> uploadReceiptImage(Long expenseId, Long petId, String userEmail,
            MultipartFile receiptImage) throws IOException;
}