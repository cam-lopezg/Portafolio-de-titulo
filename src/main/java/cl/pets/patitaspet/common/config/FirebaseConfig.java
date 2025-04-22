package cl.pets.patitaspet.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = Logger.getLogger(FirebaseConfig.class.getName());

    @Bean
    public Firestore firestore() throws IOException {
        try {
            // Carga el archivo de configuración de Firebase desde los recursos
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

            // Verificar que el recurso existe
            if (!resource.exists()) {
                throw new IOException("No se pudo encontrar el archivo firebase-service-account.json en resources");
            }

            InputStream serviceAccount = resource.getInputStream();

            // Configura Firebase con las credenciales
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Inicializa la aplicación Firebase si no está ya inicializada
            List<FirebaseApp> apps = FirebaseApp.getApps();
            if (apps == null || apps.isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Aplicación Firebase inicializada correctamente");
            }

            // Retorna una instancia de Firestore
            return FirestoreClient.getFirestore();
        } catch (IOException e) {
            logger.severe("Error al inicializar Firebase: " + e.getMessage());
            throw e;
        }
    }
}