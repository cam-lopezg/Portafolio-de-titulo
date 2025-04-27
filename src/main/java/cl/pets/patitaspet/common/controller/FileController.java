package cl.pets.patitaspet.common.controller;

import cl.pets.patitaspet.common.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Endpoint para subir archivos
     * 
     * @param file      Archivo a subir
     * @param directory Directorio donde guardar el archivo (opcional)
     * @return URL del archivo guardado
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "default") String directory) {

        try {
            String fileUrl = fileStorageService.storeFile(file, directory);

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para eliminar archivos
     * 
     * @param directory Directorio del archivo
     * @param fileName  Nombre del archivo
     * @return Estado de la operación
     */
    @DeleteMapping("/{directory}/{fileName}")
    public ResponseEntity<Map<String, Boolean>> deleteFile(
            @PathVariable String directory,
            @PathVariable String fileName) {

        String filePath = directory + "/" + fileName;
        boolean deleted = fileStorageService.deleteFile(filePath);

        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);

        if (deleted) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}