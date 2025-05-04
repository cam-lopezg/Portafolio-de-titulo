package cl.pets.patitaspet.appointment.repository;

import cl.pets.patitaspet.appointment.entity.ImportantDate;
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
public class ImportantDateRepositoryImpl implements ImportantDateRepository {

    private static final Logger logger = Logger.getLogger(ImportantDateRepositoryImpl.class.getName());
    private final Firestore firestore;
    private final String COLLECTION_NAME = "importantDates";

    @Autowired
    public ImportantDateRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
        logger.info("ImportantDateRepository inicializado con éxito");
    }

    @Override
    public ImportantDate save(ImportantDate importantDate) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();

            if (importantDate.getId() == null) {
                long numericId = System.currentTimeMillis();
                importantDate.setId(numericId);
                logger.info("ID numérico asignado a la fecha importante: " + numericId);
            }

            ApiFuture<WriteResult> result = docRef.set(importantDate);
            result.get();

            logger.info("Fecha importante guardada exitosamente con ID: " + docRef.getId());
            return importantDate;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar fecha importante en Firestore: " + e.getMessage(), e);
            throw new RuntimeException("Error al guardar fecha importante en Firestore", e);
        }
    }

    @Override
    public List<ImportantDate> findAll() {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<ImportantDate> importantDates = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                ImportantDate importantDate = document.toObject(ImportantDate.class);
                if (importantDate != null) {
                    importantDates.add(importantDate);
                } else {
                    logger.warning("No se pudo convertir documento a ImportantDate: " + document.getId());
                }
            }

            logger.info("Total de fechas importantes encontradas: " + importantDates.size());
            return importantDates;
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error al listar fechas importantes: " + e.getMessage(), e);
            throw new RuntimeException("Error al listar fechas importantes en Firestore", e);
        }
    }
}
