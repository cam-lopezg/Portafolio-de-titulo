package cl.pets.patitaspet.common.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService() {
        // Define la ubicación raíz de almacenamiento
        this.rootLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            // Crea el directorio si no existe
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento", e);
        }
    }

    /**
     * Almacena un archivo en el sistema de archivos local.
     * 
     * @param file         El archivo a almacenar
     * @param subdirectory El subdirectorio donde almacenar el archivo (opcional)
     * @return La URL relativa para acceder al archivo
     * @throws IOException Si hay un error al guardar el archivo
     */
    public String storeFile(MultipartFile file, String subdirectory) throws IOException {
        // Validar el archivo
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No se puede almacenar un archivo vacío");
        }

        Path targetLocation = this.rootLocation;

        // Si hay un subdirectorio especificado, lo crea
        if (subdirectory != null && !subdirectory.isEmpty()) {
            targetLocation = this.rootLocation.resolve(subdirectory);
            Files.createDirectories(targetLocation);
        }

        // Genera un nombre único para el archivo para evitar colisiones
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path destinationPath = targetLocation.resolve(fileName);

        // Guarda el archivo
        Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        String storedFilePath = (subdirectory != null && !subdirectory.isEmpty())
                ? subdirectory + "/" + fileName
                : fileName;

        // Devuelve la URL relativa del archivo guardado
        return "/uploads/" + storedFilePath;
    }

    /**
     * Elimina un archivo del sistema de archivos local.
     * 
     * @param filePath La ruta relativa del archivo a eliminar (sin el prefijo
     *                 "/uploads/")
     * @return true si el archivo fue eliminado, false si no existía
     */
    public boolean deleteFile(String filePath) {
        try {
            // Si la ruta incluye el prefijo "/uploads/", lo eliminamos
            if (filePath.startsWith("/uploads/")) {
                filePath = filePath.substring("/uploads/".length());
            }

            Path file = rootLocation.resolve(filePath);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un nombre único para el archivo usando UUID.
     * 
     * @param originalFileName El nombre original del archivo
     * @return Un nombre de archivo único con la extensión original
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = StringUtils.getFilenameExtension(originalFileName);
        return UUID.randomUUID().toString() + (extension != null ? "." + extension : "");
    }
}