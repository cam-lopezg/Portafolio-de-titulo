package cl.pets.patitaspet.pet.repository;

import cl.pets.patitaspet.common.util.FirestoreDateConverter;
import cl.pets.patitaspet.pet.entity.PetVaccination;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class FirestorePetVaccinationRepository implements PetVaccinationRepository {

    private static final String COLLECTION_NAME = "pet_vaccinations";
    private static final Logger logger = Logger.getLogger(FirestorePetVaccinationRepository.class.getName());

    @Autowired
    private Firestore firestore;

    @Autowired
    private FirestoreDateConverter dateConverter;

    @Override
    public CompletableFuture<PetVaccination> save(PetVaccination vaccination) {
        CompletableFuture<PetVaccination> future = new CompletableFuture<>();

        try {
            // Si es un nuevo registro, generamos un ID
            if (vaccination.getId() == null) {
                // Asignar un ID numérico basado en timestamp, igual que en Pet
                long numericId = System.currentTimeMillis();
                vaccination.setId(numericId);
                logger.info("ID numérico asignado a la vacunación: " + numericId);

                // Asignar fecha de creación
                vaccination.setCreatedAtStr(dateConverter.toString(LocalDateTime.now()));

                // Crear un documento con ID alfanumérico de Firestore
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
                logger.info("Documento creado en colección " + COLLECTION_NAME + " con ID: " + docRef.getId());

                // Persistir en Firestore
                ApiFuture<WriteResult> result = docRef.set(vaccination);
                result.get(); // Esperamos a que termine la operación
                logger.info("Vacunación guardada con éxito. ID numérico: " + numericId +
                        ", Document ID: " + docRef.getId());
            } else {
                // Es una actualización - Buscar el documento por el campo id numérico
                logger.info("Actualizando vacunación con ID numérico: " + vaccination.getId());

                // Para actualizar necesitamos primero encontrar el documento por su ID numérico
                Query query = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("id", vaccination.getId())
                        .limit(1);

                ApiFuture<QuerySnapshot> querySnapshot = query.get();
                List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

                if (!documents.isEmpty()) {
                    // Actualizar el documento existente usando su Document ID de Firestore
                    String documentId = documents.get(0).getId();
                    DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(documentId);
                    ApiFuture<WriteResult> result = docRef.set(vaccination);
                    result.get();
                    logger.info("Vacunación actualizada con éxito. ID numérico: " + vaccination.getId() +
                            ", Document ID: " + documentId);
                } else {
                    logger.warning("No se encontró la vacunación con ID numérico: " + vaccination.getId());
                    throw new IllegalArgumentException("No se encontró la vacunación con ID: " + vaccination.getId());
                }
            }

            future.complete(vaccination);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar vacunación: " + e.getMessage(), e);
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<List<PetVaccination>> findAllByPetId(Long petId) {
        CompletableFuture<List<PetVaccination>> future = new CompletableFuture<>();

        try {
            Query query = firestore.collection(COLLECTION_NAME).whereEqualTo("petId", petId);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();

            List<PetVaccination> vaccinations = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
                PetVaccination vaccination = doc.toObject(PetVaccination.class);
                vaccinations.add(vaccination);
            }

            future.complete(vaccinations);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Optional<PetVaccination>> findById(Long id) {
        CompletableFuture<Optional<PetVaccination>> future = new CompletableFuture<>();

        try {
            // Buscar por el campo id numérico en lugar del Document ID
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id", id)
                    .limit(1);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (!documents.isEmpty()) {
                PetVaccination vaccination = documents.get(0).toObject(PetVaccination.class);
                future.complete(Optional.ofNullable(vaccination));
            } else {
                future.complete(Optional.empty());
            }
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> deleteById(Long id) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            // Buscar primero por el campo id numérico
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id", id)
                    .limit(1);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (!documents.isEmpty()) {
                // Obtener el Document ID y usarlo para eliminar
                String documentId = documents.get(0).getId();
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(documentId);
                ApiFuture<WriteResult> writeResult = docRef.delete();
                writeResult.get();
                future.complete(null);
            } else {
                throw new IllegalArgumentException("No se encontró la vacunación con ID: " + id);
            }
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    @Override
    public CompletableFuture<Boolean> existsByIdAndPetId(Long id, Long petId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            // Buscar por el campo id numérico y petId
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id", id)
                    .whereEqualTo("petId", petId)
                    .limit(1);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            future.complete(!documents.isEmpty());
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }
}