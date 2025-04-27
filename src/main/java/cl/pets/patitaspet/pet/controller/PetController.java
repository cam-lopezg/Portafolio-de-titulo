package cl.pets.patitaspet.pet.controller;

import cl.pets.patitaspet.pet.dto.PetCreateRequest;
import cl.pets.patitaspet.pet.dto.PetResponse;
import cl.pets.patitaspet.pet.entity.Pet;
import cl.pets.patitaspet.pet.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private static final Logger logger = Logger.getLogger(PetController.class.getName());

    @Autowired
    private PetService petService;

    /**
     * Crea una nueva mascota asociada al usuario autenticado
     */
    @PostMapping
    public ResponseEntity<?> createPet(@RequestBody PetCreateRequest petCreateRequest) {
        try {
            logger.info("Iniciando creación de mascota: " + petCreateRequest.getName());

            // Obtener el email del usuario autenticado
            String userEmail = getCurrentUserEmail();
            logger.info("Usuario autenticado para crear mascota: " + userEmail);

            // Crear la mascota asociada al usuario actual
            PetResponse response = petService.createPet(petCreateRequest, userEmail);
            logger.info("Mascota creada exitosamente con ID: " + response.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al crear mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al crear mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear la mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene todas las mascotas del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<?> getPets() {
        try {
            logger.info("Solicitando listar todas las mascotas del usuario");

            // Obtener el email del usuario autenticado
            String userEmail = getCurrentUserEmail();
            logger.info("Usuario autenticado para listar mascotas: " + userEmail);

            // Obtener las mascotas del usuario
            List<PetResponse> pets = petService.getPetsByUser(userEmail);
            logger.info("Se encontraron " + pets.size() + " mascotas para el usuario");

            return ResponseEntity.ok(pets);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al listar mascotas: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al listar mascotas: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener las mascotas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene una mascota específica por ID, verificando que pertenezca al usuario
     * autenticado
     */
    @GetMapping("/{petId}")
    public ResponseEntity<?> getPetById(@PathVariable Long petId) {
        try {
            logger.info("Solicitando información de mascota con ID: " + petId);

            // Obtener el email del usuario autenticado
            String userEmail = getCurrentUserEmail();
            logger.info("Usuario autenticado para consulta de mascota: " + userEmail);

            // Obtener la mascota por ID (el servicio verifica que pertenezca al usuario)
            PetResponse pet = petService.getPetById(petId, userEmail);
            logger.info("Mascota encontrada: " + pet.getName() + " (ID: " + pet.getId() + ")");

            return ResponseEntity.ok(pet);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al buscar mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado a mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al obtener mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener la mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Sube o actualiza la imagen de una mascota
     * 
     * @param petId ID de la mascota
     * @param image Archivo de imagen
     * @return Respuesta con la URL de la imagen
     */
    @PostMapping(value = "/{petId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPetImage(
            @PathVariable Long petId,
            @RequestParam("image") MultipartFile image) {
        try {
            logger.info("Subiendo imagen para mascota con ID: " + petId);

            // Obtener el email del usuario autenticado
            String userEmail = getCurrentUserEmail();
            logger.info("Usuario autenticado para subida de imagen: " + userEmail);

            Pet updatedPet = petService.updatePetImage(petId, userEmail, image);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("petId", petId);
            response.put("photoUrl", updatedPet.getPhotoUrl());
            response.put("message", "Imagen de mascota actualizada exitosamente");

            logger.info("Imagen de mascota actualizada: " + updatedPet.getPhotoUrl());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al subir imagen: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al subir imagen: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al procesar la imagen: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al procesar la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al subir imagen: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al subir imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene la URL de la imagen de una mascota
     * 
     * @param petId ID de la mascota
     * @return URL de la imagen
     */
    @GetMapping("/{petId}/image")
    public ResponseEntity<?> getPetImage(@PathVariable Long petId) {
        try {
            logger.info("Obteniendo URL de imagen para mascota con ID: " + petId);

            // Obtener el email del usuario autenticado
            String userEmail = getCurrentUserEmail();

            String photoUrl = petService.getPetImageUrl(petId, userEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("photoUrl", photoUrl);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error al obtener imagen: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Acceso denegado al obtener imagen: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al obtener imagen: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Crea una nueva mascota con imagen en una sola operación
     */
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPetWithImage(
            @RequestPart("name") String petName,
            @RequestPart("species") String species,
            @RequestPart(value = "breedName", required = false) String breedName,
            @RequestPart(value = "gender", required = false) String gender,
            @RequestPart(value = "birthdate", required = false) String birthdate,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            logger.info("Iniciando creación de mascota con imagen: " + petName);

            // Obtener el email del usuario autenticado
            String userEmail = getCurrentUserEmail();
            logger.info("Usuario autenticado para crear mascota: " + userEmail);

            // Crear la solicitud de mascota a partir de los parámetros
            PetCreateRequest petCreateRequest = new PetCreateRequest();
            petCreateRequest.setName(petName);

            // Convertir string a enum Species
            try {
                cl.pets.patitaspet.pet.entity.Species speciesEnum = cl.pets.patitaspet.pet.entity.Species
                        .valueOf(species.toUpperCase());
                petCreateRequest.setSpecies(speciesEnum);
            } catch (IllegalArgumentException e) {
                logger.warning("Especie inválida: " + species);
                throw new IllegalArgumentException("Especie no válida. Valores permitidos: CAT, DOG, OTHER");
            }

            // Establecer nombre de la raza si se proporcionó
            if (breedName != null && !breedName.isEmpty()) {
                petCreateRequest.setBreedName(breedName);
            }

            // Convertir string a enum Gender si se proporcionó
            if (gender != null && !gender.isEmpty()) {
                try {
                    cl.pets.patitaspet.pet.entity.Gender genderEnum = cl.pets.patitaspet.pet.entity.Gender
                            .valueOf(gender.toUpperCase());
                    petCreateRequest.setGender(genderEnum);
                } catch (IllegalArgumentException e) {
                    logger.warning("Género inválido: " + gender);
                    throw new IllegalArgumentException("Género no válido. Valores permitidos: MALE, FEMALE");
                }
            }

            // Convertir string a LocalDate si se proporcionó fecha de nacimiento
            if (birthdate != null && !birthdate.isEmpty()) {
                try {
                    java.time.LocalDate birthdateObj = java.time.LocalDate.parse(birthdate);
                    petCreateRequest.setBirthdate(birthdateObj);
                } catch (Exception e) {
                    logger.warning("Formato de fecha inválido: " + birthdate);
                    throw new IllegalArgumentException("Formato de fecha inválido. Use YYYY-MM-DD");
                }
            }

            // Crear primero la mascota
            PetResponse createdPet = petService.createPet(petCreateRequest, userEmail);
            logger.info("Mascota creada exitosamente con ID: " + createdPet.getId());

            // Si se proporcionó una imagen, subirla
            if (image != null && !image.isEmpty()) {
                logger.info("Subiendo imagen para la mascota recién creada");
                Pet updatedPet = petService.updatePetImage(createdPet.getId(), userEmail, image);
                createdPet.setPhotoUrl(updatedPet.getPhotoUrl());
                logger.info("Imagen subida exitosamente: " + updatedPet.getPhotoUrl());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(createdPet);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error de validación al crear mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al procesar la imagen: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al procesar la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al crear mascota: " + e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear la mascota: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Obtiene el email del usuario actualmente autenticado
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.severe("Intento de acceso sin autenticación");
            throw new IllegalStateException("No hay usuario autenticado");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername(); // En nuestro caso, el username es el email
            logger.info("Usuario autenticado encontrado: " + email);
            return email;
        } else {
            String name = authentication.getName();
            logger.info("Usuario autenticado encontrado (no UserDetails): " + name);
            return name;
        }
    }
}