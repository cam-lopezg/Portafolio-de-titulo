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
public class FirestoreAppointmentRepositoryImpl implements FirestoreAppointmentRepository {

    private static final Logger logger = Logger.getLogger(FirestoreAppointmentRepositoryImpl.class.getName());
    private static final String COLLECTION_NAME = "appointments";

    private final Firestore firestore;

    @Autowired
    public FirestoreAppointmentRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
        logger.info("FirestoreAppointmentRepository inicializado con éxito");
    }

    @Override
    public String saveAppointment(PetAppointment appointment) {
        logger.info("Guardando cita para mascota ID: " + appointment.getPet().getId());
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            if (appointment.getId() == null) {
                // Asigna ID numérico basado en timestamp
                long numericId = System.currentTimeMillis();
                appointment.setId(numericId);
                logger.info("ID numérico asignado: " + numericId);
            }
            ApiFuture<WriteResult> writeResult = docRef.set(appointment);
            writeResult.get(); // esperar a que se complete
            logger.info("Cita guardada. Documento ID: " + docRef.getId());
            return docRef.getId();
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al guardar cita en Firestore", e);
            throw new RuntimeException("Error al guardar cita en Firestore", e);
        }
    }

    @Override
    public Optional<PetAppointment> findAppointmentById(String appointmentId) {
        logger.info("Buscando cita por ID: " + appointmentId);
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(appointmentId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot snapshot = future.get();
            if (snapshot.exists()) {
                PetAppointment appointment = snapshot.toObject(PetAppointment.class);
                return Optional.ofNullable(appointment);
            } else {
                logger.warning("No se encontró cita con ID: " + appointmentId);
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al buscar cita en Firestore", e);
            throw new RuntimeException("Error al buscar cita en Firestore", e);
        }
    }

    @Override
    public List<PetAppointment> findAppointmentsByPetId(Long petId) {
        logger.info("Buscando citas para mascota ID: " + petId);
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("pet.id", petId);
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            List<PetAppointment> appointments = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                PetAppointment appt = doc.toObject(PetAppointment.class);
                if (appt != null) {
                    appointments.add(appt);
                }
            }
            System.out.println(appointments.get(0).getId());
            return appointments;
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al listar citas por mascota", e);
            throw new RuntimeException("Error al listar citas por mascota", e);
        }
    }

    @Override
    public void deleteAppointment(String appointmentId) {
        logger.info("Eliminando cita (raw id): " + appointmentId);
        try {
            // Si es un número puro, lo tratamos como tu id interno
            if (appointmentId != null && appointmentId.matches("\\d+")) {
                long internalId = Long.parseLong(appointmentId);
                // Buscamos doc(s) donde el campo "id" == internalId
                Query query = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("id", internalId);
                ApiFuture<QuerySnapshot> future = query.get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();

                for (DocumentSnapshot doc : docs) {
                    String docId = doc.getId();
                    // Borrado por documentId
                    firestore.collection(COLLECTION_NAME)
                            .document(docId)
                            .delete()
                            .get();
                    logger.info("Cita eliminada por id interno. Document ID: " + docId);
                }

            } else {
                // Si no es puramente numérico, lo tratamos como el documentId
                firestore.collection(COLLECTION_NAME)
                        .document(appointmentId)
                        .delete()
                        .get();
                logger.info("Cita eliminada por documentId: " + appointmentId);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al eliminar cita en Firestore", e);
            throw new RuntimeException("Error al eliminar cita en Firestore", e);
        }
    }

    @Override
    public List<PetAppointment> findAllAppointments() {
        logger.info("Listando todas las citas médicas");
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> docs = future.get().getDocuments();
            List<PetAppointment> appointments = new ArrayList<>();
            for (DocumentSnapshot doc : docs) {
                PetAppointment appt = doc.toObject(PetAppointment.class);
                if (appt != null) {
                    appointments.add(appt);
                }
            }
            return appointments;
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al listar todas las citas en Firestore", e);
            throw new RuntimeException("Error al listar todas las citas en Firestore", e);
        }
    }
}
