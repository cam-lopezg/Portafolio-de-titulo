package cl.pets.patitaspet.appointment.repository;

import cl.pets.patitaspet.appointment.entity.Reminder;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class FirestoreReminderRepositoryImpl implements FirestoreReminderRepository {

    private static final Logger logger = Logger.getLogger(FirestoreReminderRepositoryImpl.class.getName());
    private final Firestore firestore;
    private final String COLLECTION_NAME = "reminders";

    @Autowired
    public FirestoreReminderRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
        logger.info("FirestoreReminderRepository inicializado con éxito");
    }

    @Override
    public String saveReminder(Reminder reminder) {
        logger.info("Iniciando guardado de recordatorio para usuario ID: " +
                (reminder.getUserId() != null ? reminder.getUserId() : "desconocido"));

        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();

            if (reminder.getId() == null) {
                long generatedId = System.currentTimeMillis();
                reminder.setId(generatedId);
                logger.info("ID numérico asignado al recordatorio: " + generatedId);
            }

            ApiFuture<WriteResult> result = docRef.set(reminder);
            result.get(); // espera que se complete la escritura

            logger.info("Recordatorio guardado exitosamente. ID doc: " + docRef.getId());
            return docRef.getId();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar recordatorio en Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al guardar recordatorio en Firestore", e);
        }
    }

    @Override
    public List<Reminder> findAllReminders() {
        logger.info("Buscando todos los recordatorios...");
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<Reminder> reminders = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents) {
                Reminder reminder = doc.toObject(Reminder.class);
                if (reminder != null) {
                    reminders.add(reminder);
                } else {
                    logger.warning("Documento no convertible a Reminder: " + doc.getId());
                }
            }

            logger.info("Recordatorios recuperados: " + reminders.size());
            return reminders;
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al recuperar recordatorios de Firestore", e);
            throw new RuntimeException("Error al recuperar recordatorios", e);
        }
    }

    @Override
    public void deleteReminder(Long id) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("id", id)
                    .limit(1);
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            if (docs.isEmpty()) {
                throw new IllegalArgumentException("No se encontró recordatorio con ID: " + id);
            }
            String docId = docs.get(0).getId();
            ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME)
                    .document(docId)
                    .delete();
            writeResult.get();
            logger.info("Recordatorio eliminado. Doc ID: " + docId);
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al eliminar recordatorio", e);
            throw new RuntimeException("Error al eliminar recordatorio", e);
        }
    }



}
