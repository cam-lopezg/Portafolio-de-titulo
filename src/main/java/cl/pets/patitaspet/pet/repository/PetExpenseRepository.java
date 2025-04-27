package cl.pets.patitaspet.pet.repository;

import cl.pets.patitaspet.pet.entity.ExpenseCategory;
import cl.pets.patitaspet.pet.entity.PetExpense;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PetExpenseRepository {

    /**
     * Guarda un nuevo gasto de mascota
     * 
     * @param expense El gasto a guardar
     * @return El gasto guardado con su ID asignado
     */
    CompletableFuture<PetExpense> save(PetExpense expense);

    /**
     * Busca todos los gastos de una mascota
     * 
     * @param petId ID de la mascota
     * @return Lista de gastos de la mascota
     */
    CompletableFuture<List<PetExpense>> findAllByPetId(Long petId);

    /**
     * Busca todos los gastos de una mascota de una categoría específica
     * 
     * @param petId    ID de la mascota
     * @param category Categoría de gastos
     * @return Lista de gastos de la categoría especificada
     */
    CompletableFuture<List<PetExpense>> findAllByPetIdAndCategory(Long petId, ExpenseCategory category);

    /**
     * Busca un gasto específico por su ID
     * 
     * @param id ID del gasto
     * @return El gasto si existe, Optional vacío si no
     */
    CompletableFuture<Optional<PetExpense>> findById(Long id);

    /**
     * Elimina un gasto por su ID
     * 
     * @param id ID del gasto a eliminar
     * @return CompletableFuture que se completa cuando la operación termina
     */
    CompletableFuture<Void> deleteById(Long id);

    /**
     * Verifica si existe un gasto con el ID dado que pertenezca a la mascota
     * indicada
     * 
     * @param id    ID del gasto
     * @param petId ID de la mascota
     * @return true si existe un gasto con ese ID para esa mascota
     */
    CompletableFuture<Boolean> existsByIdAndPetId(Long id, Long petId);

    /**
     * Actualiza la URL de la imagen de recibo de un gasto
     *
     * @param id       ID del gasto
     * @param imageUrl URL de la imagen
     * @return El gasto actualizado
     */
    CompletableFuture<PetExpense> updateReceiptImage(Long id, String imageUrl);
}