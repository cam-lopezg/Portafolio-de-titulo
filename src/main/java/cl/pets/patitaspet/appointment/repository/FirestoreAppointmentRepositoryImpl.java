package cl.pets.patitaspet.appointment.repository;

import cl.pets.patitaspet.appointment.entity.PetAppointment;
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
public class FirestoreAppointmentRepositoryImpl implements FirestoreAppointmentRepository{

    private static final Logger logger = Logger.getLogger(FirestoreAppointmentRepositoryImpl.class.getName());
    private final Firestore firestore;
    private final String COLLECTION_NAME = "appointments";

    @Autowired
    public FirestoreAppointmentRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
        logger.info("FirestoreAppointmentRepository inicializado con éxito");
    }

    @Override
    public String saveAppointment(PetAppointment appointment) {
        logger.info("Iniciando guardado de cita para mascota ID: " + appointment.getPet().getId());
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            logger.info("Documento creado en colección " + COLLECTION_NAME + " con ID: " + docRef.getId());

            // Si no tiene ID, asignar basado en timestamp
            if (appointment.getId() == null) {
                long numericId = System.currentTimeMillis();
                appointment.setId(numericId);
                logger.info("ID numérico asignado a la cita: " + numericId);
            }

            // Guardar la cita
            logger.info("Guardando datos de cita en Firestore...");

            ApiFuture<WriteResult> result = docRef.set(appointment);
            result.get(); // Esperar escritura

            logger.info("Cita guardada exitosamente. Documento ID: " + docRef.getId() + ", timestamp: "
                    + result.get().getUpdateTime());
            return docRef.getId();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar cita en Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al guardar cita en Firestore", e);
        }
    }

    @Override
    public Optional<PetAppointment> findAppointmentById(String appointmentId) {
        logger.info("Buscando cita médica por ID: " + appointmentId);
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(appointmentId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                PetAppointment appointment = document.toObject(PetAppointment.class);
                if (appointment != null) {
                    logger.info("Cita encontrada: " + appointment.getTitle() + " para mascota ID: " + appointment.getPet().getId());
                }
                return Optional.ofNullable(appointment);
            } else {
                logger.warning("No se encontró cita con ID: " + appointmentId);
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al buscar cita médica por ID: " + e.getMessage(), e);
            throw new RuntimeException("Error al buscar cita médica en Firestore", e);
        }
    }

    @Override
    public List<PetAppointment> findAppointmentsByPetId(Long petId) {
        logger.info("Buscando citas médicas para la mascota con ID: " + petId);
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("pet.id", petId)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            logger.info("Query completada. Se encontraron " + documents.size() + " citas.");

            List<PetAppointment> appointments = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                PetAppointment appointment = document.toObject(PetAppointment.class);
                if (appointment != null) {
                    logger.info("Cita encontrada: " + appointment.getTitle());
                    appointments.add(appointment);
                } else {
                    logger.warning("No se pudo convertir documento a PetAppointment: " + document.getId());
                }
            }

            return appointments;
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al buscar citas médicas para mascota: " + e.getMessage(), e);
            throw new RuntimeException("Error al buscar citas médicas en Firestore", e);
        }
    }

    @Override
    public void deleteAppointment(String appointmentId) {
        logger.info("Iniciando eliminación de cita médica con ID: " + appointmentId);
        try {
            ApiFuture<WriteResult> writeResult = firestore.collection(COLLECTION_NAME).document(appointmentId).delete();
            writeResult.get();
            logger.info("Cita médica eliminada exitosamente. ID: " + appointmentId + ", timestamp: "
                    + writeResult.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al eliminar cita médica en Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al eliminar cita médica en Firestore", e);
        }
    }

    @Override
    public List<PetAppointment> findAllAppointments() {
        logger.info("Buscando todas las citas médicas...");
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<PetAppointment> appointments = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                PetAppointment appointment = document.toObject(PetAppointment.class);
                if (appointment != null) {
                    appointments.add(appointment);
                } else {
                    logger.warning("No se pudo convertir documento a PetAppointment: " + document.getId());
                }
            }

            logger.info("Total de citas encontradas: " + appointments.size());
            return appointments;
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al listar todas las citas médicas: " + e.getMessage(), e);
            throw new RuntimeException("Error al listar todas las citas médicas en Firestore", e);
        }
    }

}



