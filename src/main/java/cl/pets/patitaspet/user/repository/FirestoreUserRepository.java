package cl.pets.patitaspet.user.repository;

import cl.pets.patitaspet.user.entity.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class FirestoreUserRepository {

    private static final Logger logger = Logger.getLogger(FirestoreUserRepository.class.getName());
    private final Firestore firestore;
    private final String COLLECTION_NAME = "users";

    @Autowired
    public FirestoreUserRepository(Firestore firestore) {
        this.firestore = firestore;
        logger.info("FirestoreUserRepository inicializado con éxito");
    }

    public String saveUser(User user) {
        try {
            // Crear un documento nuevo en la colección "users"
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();

            // Si el usuario no tiene ID, le asignamos un ID numérico basado en timestamp
            if (user.getId() == null) {
                // En lugar de convertir el ID a Long, usamos un timestamp como ID numérico
                long numericId = System.currentTimeMillis();
                user.setId(numericId);
                logger.info("ID numérico asignado al usuario: " + numericId);
            }

            // Guardar el usuario en Firestore
            ApiFuture<WriteResult> result = docRef.set(user);
            result.get();

            logger.info("Usuario guardado con ID: " + docRef.getId());
            // Retornar el ID del documento creado
            return docRef.getId();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar usuario en Firestore", e);
            throw new RuntimeException("Error al guardar usuario en Firestore", e);
        }
    }

    public Optional<User> findUserById(String userId) {
        try {
            // Obtener referencia al documento
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);

            // Obtener el documento
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            // Verificar si el documento existe
            if (document.exists()) {
                User user = document.toObject(User.class);
                return Optional.ofNullable(user);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al buscar usuario por ID en Firestore", e);
        }
    }

    public List<User> findAllUsers() {
        try {
            // Obtener todos los documentos de la colección
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                User user = document.toObject(User.class);
                users.add(user);
            }
            return users;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al obtener todos los usuarios de Firestore", e);
        }
    }

    public Optional<User> findUserByEmail(String email) {
        try {
            // Crear consulta para buscar usuario por email
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                User user = documents.get(0).toObject(User.class);
                return Optional.ofNullable(user);
            } else {
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al buscar usuario por email en Firestore", e);
        }
    }

    public void deleteUser(String userId) {
        try {
            // Eliminar documento por ID
            ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME).document(userId).delete();

            // Esperar a que se complete la operación
            writeResult.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al eliminar usuario de Firestore", e);
        }
    }

    public void updateUser(String userId, User user) {
        try {
            // Actualizar documento por ID
            ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME).document(userId).set(user);

            // Esperar a que se complete la operación
            writeResult.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al actualizar usuario en Firestore", e);
        }
    }

    public void updateUserFields(String userId, Map<String, Object> fields) {
        try {
            // Actualizar campos específicos del documento
            ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME).document(userId).update(fields);

            // Esperar a que se complete la operación
            writeResult.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error al actualizar campos de usuario en Firestore", e);
        }
    }

    /**
     * Actualiza un usuario usando el objeto User directamente.
     * Busca el documento por el email del usuario.
     */
    public void updateUser(User user) {
        try {
            logger.info("Actualizando usuario con ID: " + user.getId() + " y email: " + user.getEmail());

            // Buscar el documento del usuario por email
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", user.getEmail())
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                String documentId = documents.get(0).getId();
                logger.info("Documento encontrado para usuario con ID: " + documentId);

                // Actualizar el documento
                ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME).document(documentId)
                        .set(user);
                WriteResult result = writeResult.get();

                logger.info("Usuario actualizado con éxito. Timestamp: " + result.getUpdateTime());
            } else {
                logger.warning("No se pudo encontrar el documento para el usuario con email: " + user.getEmail());
                throw new RuntimeException(
                        "No se pudo encontrar el documento para el usuario con email: " + user.getEmail());
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al actualizar usuario en Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al actualizar usuario en Firestore", e);
        }
    }

    /**
     * Busca un usuario por su ID numérico (el campo "id" dentro del documento).
     * 
     * @param numericId ID numérico del usuario
     * @return Optional con el usuario si existe, empty si no
     */
    public Optional<User> findUserByNumericId(Long numericId) {
        try {
            logger.info("Buscando usuario por ID numérico: " + numericId);

            // Crear consulta para buscar usuario por su campo id numérico
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id", numericId)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                User user = documents.get(0).toObject(User.class);
                logger.info("Usuario encontrado por ID numérico: " + numericId);
                return Optional.ofNullable(user);
            } else {
                logger.warning("No se encontró usuario con ID numérico: " + numericId);
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al buscar usuario por ID numérico en Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al buscar usuario por ID numérico en Firestore", e);
        }
    }
}