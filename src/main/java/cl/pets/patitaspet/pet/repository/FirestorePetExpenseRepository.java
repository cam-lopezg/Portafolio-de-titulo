package cl.pets.patitaspet.pet.repository;

import cl.pets.patitaspet.common.util.FirestoreDateConverter;
import cl.pets.patitaspet.pet.entity.ExpenseCategory;
import cl.pets.patitaspet.pet.entity.PetExpense;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class FirestorePetExpenseRepository implements PetExpenseRepository {

    private static final String COLLECTION_NAME = "pet_expenses";
    private static final Logger logger = Logger.getLogger(FirestorePetExpenseRepository.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private Firestore firestore;

    @Autowired
    private FirestoreDateConverter dateConverter;

    @Override
    public CompletableFuture<PetExpense> save(PetExpense expense) {
        CompletableFuture<PetExpense> future = new CompletableFuture<>();

        try {
            // Si es un nuevo registro, generar un ID único
            if (expense.getId() == null) {
                expense.setId(System.currentTimeMillis());
            }

            // Establecer fecha de creación si es nueva
            if (expense.getCreatedAt() == null) {
                LocalDateTime now = LocalDateTime.now();
                expense.setCreatedAt(now);
                expense.setCreatedAtStr(now.format(DATETIME_FORMATTER));
            }

            // Convertir BigDecimal a String para Firestore
            if (expense.getAmount() != null) {
                expense.setAmountStr(expense.getAmount().toString());
            }

            // Crear mapa para Firestore
            Map<String, Object> expenseData = new HashMap<>();
            expenseData.put("id", expense.getId());
            expenseData.put("petId", expense.getPetId());
            expenseData.put("title", expense.getTitle());
            expenseData.put("description", expense.getDescription());
            expenseData.put("amountStr", expense.getAmountStr());
            expenseData.put("currencyCode", expense.getCurrencyCode());
            expenseData.put("category", expense.getCategory() != null ? expense.getCategory().name() : null);
            expenseData.put("dateStr", expense.getDateStr());
            expenseData.put("receiptImageUrl", expense.getReceiptImageUrl());
            expenseData.put("vendorName", expense.getVendorName());
            expenseData.put("createdAtStr", expense.getCreatedAtStr());

            // Guardar en Firestore
            String documentId = String.valueOf(expense.getId());
            ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME).document(documentId).set(expenseData);

            // Esperar a que la operación termine
            result.get();

            future.complete(expense);
            logger.info("Gasto guardado con ID: " + expense.getId());
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al guardar gasto en Firestore", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<List<PetExpense>> findAllByPetId(Long petId) {
        CompletableFuture<List<PetExpense>> future = new CompletableFuture<>();

        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("petId", petId);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            List<PetExpense> expenses = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                PetExpense expense = documentToExpense(document);
                expenses.add(expense);
            }

            future.complete(expenses);
            logger.info("Se encontraron " + expenses.size() + " gastos para la mascota: " + petId);
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al buscar gastos por mascota en Firestore", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<List<PetExpense>> findAllByPetIdAndCategory(Long petId, ExpenseCategory category) {
        CompletableFuture<List<PetExpense>> future = new CompletableFuture<>();

        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("petId", petId)
                    .whereEqualTo("category", category.name());

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            List<PetExpense> expenses = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                PetExpense expense = documentToExpense(document);
                expenses.add(expense);
            }

            future.complete(expenses);
            logger.info("Se encontraron " + expenses.size() + " gastos para la mascota: " + petId +
                    " en la categoría: " + category.name());
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al buscar gastos por mascota y categoría en Firestore", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Optional<PetExpense>> findById(Long id) {
        CompletableFuture<Optional<PetExpense>> future = new CompletableFuture<>();

        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(String.valueOf(id));
            ApiFuture<DocumentSnapshot> documentSnapshot = docRef.get();
            DocumentSnapshot document = documentSnapshot.get();

            if (document.exists()) {
                PetExpense expense = documentToExpense(document);
                future.complete(Optional.of(expense));
                logger.info("Gasto encontrado con ID: " + id);
            } else {
                future.complete(Optional.empty());
                logger.info("No se encontró gasto con ID: " + id);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al buscar gasto por ID en Firestore", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> deleteById(Long id) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(String.valueOf(id));
            ApiFuture<WriteResult> writeResult = docRef.delete();
            writeResult.get();
            future.complete(null);
            logger.info("Gasto eliminado con ID: " + id);
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al eliminar gasto en Firestore", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> existsByIdAndPetId(Long id, Long petId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(String.valueOf(id));
            ApiFuture<DocumentSnapshot> documentSnapshot = docRef.get();
            DocumentSnapshot document = documentSnapshot.get();

            if (document.exists()) {
                Long documentPetId = document.getLong("petId");
                boolean exists = documentPetId != null && documentPetId.equals(petId);
                future.complete(exists);
                logger.info(
                        "Verificación de existencia de gasto: " + exists + " (ID: " + id + ", PetID: " + petId + ")");
            } else {
                future.complete(false);
                logger.info("No se encontró el gasto con ID: " + id);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al verificar existencia de gasto en Firestore", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<PetExpense> updateReceiptImage(Long id, String imageUrl) {
        CompletableFuture<PetExpense> future = new CompletableFuture<>();

        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(String.valueOf(id));

            // Actualizar solo el campo de la URL de la imagen
            Map<String, Object> updates = new HashMap<>();
            updates.put("receiptImageUrl", imageUrl);

            ApiFuture<WriteResult> writeResult = docRef.update(updates);
            writeResult.get();

            // Obtener el documento actualizado
            ApiFuture<DocumentSnapshot> documentSnapshot = docRef.get();
            DocumentSnapshot document = documentSnapshot.get();

            if (document.exists()) {
                PetExpense updatedExpense = documentToExpense(document);
                future.complete(updatedExpense);
                logger.info("URL de recibo actualizada para gasto ID: " + id);
            } else {
                future.completeExceptionally(new NoSuchElementException("No se encontró el gasto con ID: " + id));
                logger.warning("No se encontró el gasto con ID: " + id + " después de actualizar la URL del recibo");
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al actualizar URL de recibo en Firestore", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    // Método auxiliar para convertir un documento de Firestore a un objeto
    // PetExpense
    private PetExpense documentToExpense(DocumentSnapshot document) {
        PetExpense expense = new PetExpense();

        // Datos básicos
        expense.setId(document.getLong("id"));
        expense.setPetId(document.getLong("petId"));
        expense.setTitle(document.getString("title"));
        expense.setDescription(document.getString("description"));

        // Manejo de importe
        String amountStr = document.getString("amountStr");
        if (amountStr != null && !amountStr.isEmpty()) {
            expense.setAmountStr(amountStr);
            try {
                expense.setAmount(new BigDecimal(amountStr));
            } catch (NumberFormatException e) {
                logger.warning("Error al convertir el importe a BigDecimal: " + amountStr);
            }
        }

        expense.setCurrencyCode(document.getString("currencyCode"));

        // Categoría
        String categoryStr = document.getString("category");
        if (categoryStr != null && !categoryStr.isEmpty()) {
            try {
                expense.setCategory(ExpenseCategory.valueOf(categoryStr));
            } catch (IllegalArgumentException e) {
                logger.warning("Categoría de gasto inválida: " + categoryStr);
            }
        }

        expense.setDateStr(document.getString("dateStr"));
        expense.setReceiptImageUrl(document.getString("receiptImageUrl"));
        expense.setVendorName(document.getString("vendorName"));
        expense.setCreatedAtStr(document.getString("createdAtStr"));

        // Intentar convertir la fecha de creación a LocalDateTime
        if (expense.getCreatedAtStr() != null) {
            try {
                LocalDateTime createdAt = LocalDateTime.parse(expense.getCreatedAtStr(), DATETIME_FORMATTER);
                expense.setCreatedAt(createdAt);
            } catch (Exception e) {
                logger.warning("Error al parsear la fecha de creación: " + expense.getCreatedAtStr());
            }
        }

        return expense;
    }
}