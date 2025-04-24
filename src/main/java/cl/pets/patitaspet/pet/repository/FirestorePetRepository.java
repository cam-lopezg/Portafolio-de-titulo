package cl.pets.patitaspet.pet.repository;

import cl.pets.patitaspet.pet.entity.Pet;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class FirestorePetRepository {

    private static final Logger logger = Logger.getLogger(FirestorePetRepository.class.getName());
    private final Firestore firestore;
    private final String COLLECTION_NAME = "pets";

    @Autowired
    public FirestorePetRepository(Firestore firestore) {
        this.firestore = firestore;
        logger.info("FirestorePetRepository inicializado con éxito");
    }

    public String savePet(Pet pet) {
        logger.info("Iniciando guardado de mascota: " + pet.getName());
        try {
            // Crear un documento nuevo en la colección "pets"
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            logger.info("Documento creado en colección " + COLLECTION_NAME + " con ID: " + docRef.getId());

            // Si la mascota no tiene ID, le asignamos un ID numérico basado en timestamp
            if (pet.getId() == null) {
                long numericId = System.currentTimeMillis();
                pet.setId(numericId);
                logger.info("ID numérico asignado a la mascota: " + numericId);
            }

            // Guardar la mascota en Firestore
            logger.info("Guardando datos de mascota en Firestore...");

            // Log detallado de los datos a guardar
            logger.info("Datos de mascota a guardar - Nombre: " + pet.getName() +
                    ", Especie: " + pet.getSpecies() +
                    ", ID: " + pet.getId() +
                    ", Usuario ID: " + pet.getUserId());

            ApiFuture<WriteResult> result = docRef.set(pet);
            result.get();

            logger.info("Mascota guardada con éxito. Documento ID: " + docRef.getId() + ", timestamp: "
                    + result.get().getUpdateTime());
            // Retornar el ID del documento creado
            return docRef.getId();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar mascota en Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al guardar mascota en Firestore", e);
        }
    }

    public Optional<Pet> findPetById(String petId) {
        logger.info("Buscando mascota por ID: " + petId);
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(petId);
            logger.info("Referencia de documento obtenida para ID: " + petId);

            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                logger.info("Documento encontrado para mascota ID: " + petId);
                Pet pet = document.toObject(Pet.class);

                if (pet != null) {
                    logger.info("Mascota encontrada: " + pet.getName() + ", ID: " + pet.getId());
                    logger.info("Mascota pertenece al usuario: " + pet.getUserId());
                } else {
                    logger.warning("Error al convertir documento a objeto Pet");
                }

                return Optional.ofNullable(pet);
            } else {
                logger.warning("No se encontró documento para mascota ID: " + petId);
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al buscar mascota por ID en Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al buscar mascota por ID en Firestore", e);
        }
    }

    public List<Pet> findPetsByUserId(Long userId) {
        logger.info("Buscando mascotas para el usuario con ID: " + userId);
        try {
            // Actualizado para usar el nuevo campo userId en lugar de user.id
            logger.info("Ejecutando query: whereEqualTo(\"userId\", " + userId + ")");

            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            logger.info("Query completada. Encontrados " + documents.size() + " documentos");

            List<Pet> pets = new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {
                Pet pet = document.toObject(Pet.class);
                if (pet != null) {
                    logger.info("Mascota encontrada en resultado: " + pet.getName() + ", ID: " + pet.getId());
                    pets.add(pet);
                } else {
                    logger.warning("No se pudo convertir documento a objeto Pet: " + document.getId());
                }
            }

            logger.info("Total de mascotas encontradas para el usuario " + userId + ": " + pets.size());
            return pets;
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al buscar mascotas por ID de usuario en Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al buscar mascotas por ID de usuario en Firestore", e);
        }
    }

    public void deletePet(String petId) {
        logger.info("Iniciando eliminación de mascota con ID: " + petId);
        try {
            ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME).document(petId).delete();
            writeResult.get();
            logger.info("Mascota eliminada exitosamente. ID: " + petId + ", timestamp: "
                    + writeResult.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al eliminar mascota de Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al eliminar mascota de Firestore", e);
        }
    }
}