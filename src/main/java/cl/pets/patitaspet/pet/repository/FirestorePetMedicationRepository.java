package cl.pets.patitaspet.pet.repository;

import cl.pets.patitaspet.common.util.FirestoreDateConverter;
import cl.pets.patitaspet.pet.entity.Pet;
import cl.pets.patitaspet.pet.entity.PetMedication;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class FirestorePetMedicationRepository implements PetMedicationRepository {

    private static final String COLLECTION_NAME = "pet_medications";
    private static final Logger logger = Logger.getLogger(FirestorePetMedicationRepository.class.getName());

    @Autowired
    private Firestore firestore;

    @Autowired
    private FirestorePetRepository petRepository;

    @Autowired
    private FirestoreDateConverter dateConverter;

    @Override
    public CompletableFuture<Long> saveMedication(PetMedication medication) {
        CompletableFuture<Long> future = new CompletableFuture<>();

        try {
            // Asignar ID si no tiene uno
            if (medication.getId() == null) {
                // Generar un ID numérico basado en timestamp para consistencia con el resto del
                // proyecto
                long numericId = System.currentTimeMillis();
                medication.setId(numericId);
                logger.info("ID numérico asignado al medicamento: " + numericId);
            }

            logger.info("Guardando medicamento con ID: " + medication.getId());

            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();

            // Crear el mapa de datos para guardar en Firestore
            Map<String, Object> data = Map.of(
                    "id", medication.getId(),
                    "petId", medication.getPet().getId(),
                    "medicationName", medication.getMedicationName(),
                    "startDate", medication.getStartDate() != null ? medication.getStartDate().toString() : null,
                    "endDate", medication.getEndDate() != null ? medication.getEndDate().toString() : null,
                    "dosageInstructions",
                    medication.getDosageInstructions() != null ? medication.getDosageInstructions() : "",
                    "notes", medication.getNotes() != null ? medication.getNotes() : "");

            // Guardar el documento
            ApiFuture<WriteResult> result = docRef.set(data);
            result.get(); // Esperar a que termine la operación

            future.complete(medication.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar el medicamento", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<List<PetMedication>> findMedicationsByPetId(Long petId) {
        CompletableFuture<List<PetMedication>> future = new CompletableFuture<>();

        try {
            // Buscar por el campo petId
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("petId", petId);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
            List<PetMedication> medications = new ArrayList<>();

            CompletableFuture<Optional<Pet>> petFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return petRepository.findPetByNumericId(petId);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error al buscar mascota para medicamentos", e);
                    return Optional.empty();
                }
            });

            petFuture.thenAccept(petOpt -> {
                if (petOpt.isEmpty()) {
                    future.complete(new ArrayList<>());
                    return;
                }

                Pet pet = petOpt.get();

                for (QueryDocumentSnapshot document : documents) {
                    PetMedication medication = new PetMedication();
                    medication.setId(document.getLong("id"));
                    medication.setPet(pet);
                    medication.setMedicationName(document.getString("medicationName"));

                    // Convertir fechas de String a LocalDate
                    String startDateStr = document.getString("startDate");
                    if (startDateStr != null && !startDateStr.isEmpty()) {
                        medication.setStartDate(LocalDate.parse(startDateStr));
                    }

                    String endDateStr = document.getString("endDate");
                    if (endDateStr != null && !endDateStr.isEmpty()) {
                        medication.setEndDate(LocalDate.parse(endDateStr));
                    }

                    medication.setDosageInstructions(document.getString("dosageInstructions"));
                    medication.setNotes(document.getString("notes"));

                    medications.add(medication);
                }

                future.complete(medications);
            }).exceptionally(e -> {
                logger.log(Level.SEVERE, "Error al procesar medicamentos", e);
                future.completeExceptionally(e);
                return null;
            });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al buscar medicamentos por petId", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Optional<PetMedication>> findMedicationById(Long medicationId) {
        CompletableFuture<Optional<PetMedication>> future = new CompletableFuture<>();

        try {
            // Buscar por el campo id numérico en lugar del Document ID
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id", medicationId)
                    .limit(1);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (documents.isEmpty()) {
                logger.info("No se encontró el medicamento con ID: " + medicationId);
                future.complete(Optional.empty());
                return future;
            }

            DocumentSnapshot document = documents.get(0);

            // Obtener la referencia a la mascota
            Long petId = document.getLong("petId");
            if (petId == null) {
                logger.warning("Medicamento sin referencia a mascota. ID: " + medicationId);
                future.complete(Optional.empty());
                return future;
            }

            // Buscar la mascota
            CompletableFuture<Optional<Pet>> petFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return petRepository.findPetByNumericId(petId);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error al buscar mascota para medicamento", e);
                    return Optional.empty();
                }
            });

            petFuture.thenAccept(petOpt -> {
                if (petOpt.isEmpty()) {
                    future.complete(Optional.empty());
                    return;
                }

                Pet pet = petOpt.get();

                // Crear el objeto medicamento
                PetMedication medication = new PetMedication();
                medication.setId(document.getLong("id"));
                medication.setPet(pet);
                medication.setMedicationName(document.getString("medicationName"));

                // Convertir fechas de String a LocalDate
                String startDateStr = document.getString("startDate");
                if (startDateStr != null && !startDateStr.isEmpty()) {
                    medication.setStartDate(LocalDate.parse(startDateStr));
                }

                String endDateStr = document.getString("endDate");
                if (endDateStr != null && !endDateStr.isEmpty()) {
                    medication.setEndDate(LocalDate.parse(endDateStr));
                }

                medication.setDosageInstructions(document.getString("dosageInstructions"));
                medication.setNotes(document.getString("notes"));

                future.complete(Optional.of(medication));

            }).exceptionally(e -> {
                logger.log(Level.SEVERE, "Error al procesar medicamento", e);
                future.completeExceptionally(e);
                return null;
            });

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al buscar medicamento por ID", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> updateMedication(PetMedication medication) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (medication.getId() == null) {
            future.completeExceptionally(new IllegalArgumentException("No se puede actualizar un medicamento sin ID"));
            return future;
        }

        try {
            // Buscar por el campo id numérico
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id", medication.getId())
                    .limit(1);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (documents.isEmpty()) {
                logger.warning("No se encontró el medicamento con ID: " + medication.getId());
                future.complete(false);
                return future;
            }

            // Obtener el Document ID y usarlo para actualizar
            String documentId = documents.get(0).getId();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(documentId);

            // Crear el mapa de datos para actualizar
            Map<String, Object> data = Map.of(
                    "petId", medication.getPet().getId(),
                    "medicationName", medication.getMedicationName(),
                    "startDate", medication.getStartDate() != null ? medication.getStartDate().toString() : null,
                    "endDate", medication.getEndDate() != null ? medication.getEndDate().toString() : null,
                    "dosageInstructions",
                    medication.getDosageInstructions() != null ? medication.getDosageInstructions() : "",
                    "notes", medication.getNotes() != null ? medication.getNotes() : "");

            // Actualizar el documento
            ApiFuture<WriteResult> updateResult = docRef.update(data);
            updateResult.get(); // Esperar a que termine la operación

            logger.info("Medicamento actualizado correctamente. ID: " + medication.getId());
            future.complete(true);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al actualizar medicamento", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> deleteMedication(Long medicationId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // Buscar por el campo id numérico
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id", medicationId)
                    .limit(1);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (documents.isEmpty()) {
                logger.warning("No se encontró el medicamento con ID: " + medicationId);
                future.complete(null); // Consideramos éxito si no existía
                return future;
            }

            // Obtener el Document ID y usarlo para eliminar
            String documentId = documents.get(0).getId();
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(documentId);

            // Eliminar el documento
            ApiFuture<WriteResult> deleteResult = docRef.delete();
            deleteResult.get(); // Esperar a que termine la operación

            logger.info("Medicamento eliminado correctamente. ID: " + medicationId);
            future.complete(null);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al eliminar medicamento", e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> existsByIdAndPetId(Long medicationId, Long petId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            // Buscar por el campo id y petId
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id", medicationId)
                    .whereEqualTo("petId", petId)
                    .limit(1);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            future.complete(!documents.isEmpty());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al verificar existencia de medicamento", e);
            future.completeExceptionally(e);
        }

        return future;
    }
}