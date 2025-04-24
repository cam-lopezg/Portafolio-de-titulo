package cl.pets.patitaspet.pet.service;

import cl.pets.patitaspet.common.util.DateUtil;
import cl.pets.patitaspet.pet.dto.PetCreateRequest;
import cl.pets.patitaspet.pet.dto.PetResponse;
import cl.pets.patitaspet.pet.entity.Breed;
import cl.pets.patitaspet.pet.entity.Pet;
import cl.pets.patitaspet.pet.repository.FirestorePetRepository;
import cl.pets.patitaspet.user.entity.User;
import cl.pets.patitaspet.user.repository.FirestoreUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class PetServiceImpl implements PetService {

    private static final Logger logger = Logger.getLogger(PetServiceImpl.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final FirestorePetRepository petRepository;
    private final FirestoreUserRepository userRepository;

    @Autowired
    public PetServiceImpl(FirestorePetRepository petRepository, FirestoreUserRepository userRepository) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        logger.info("PetServiceImpl inicializado");
    }

    @Override
    public PetResponse createPet(PetCreateRequest request, String userEmail) {
        logger.info("Iniciando proceso de creación de mascota para usuario: " + userEmail);

        if (request == null) {
            logger.warning("Request de creación de mascota es nulo");
            throw new IllegalArgumentException("La solicitud no puede ser nula.");
        }

        // Validar campos requeridos
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            logger.warning("Nombre de mascota vacío en la solicitud");
            throw new IllegalArgumentException("El nombre de la mascota no puede estar vacío.");
        }

        if (request.getSpecies() == null) {
            logger.warning("Especie de mascota no especificada en la solicitud");
            throw new IllegalArgumentException("La especie de la mascota no puede estar vacía.");
        }

        logger.info("Buscando usuario por email: " + userEmail);
        // Buscar el usuario por email
        Optional<User> userOpt = userRepository.findUserByEmail(userEmail);
        if (userOpt.isEmpty()) {
            logger.warning("No se encontró el usuario con email: " + userEmail);
            throw new IllegalArgumentException("Usuario no encontrado para el email: " + userEmail);
        }

        User user = userOpt.get();
        logger.info("Usuario encontrado con ID: " + user.getId());

        // Crear la mascota
        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());

        logger.info("Creando mascota: " + request.getName() + " de especie: " + request.getSpecies());

        // Crear objeto Breed si se proporciona un nombre de raza
        if (request.getBreedName() != null && !request.getBreedName().isEmpty()) {
            Breed breed = new Breed();
            breed.setName(request.getBreedName());
            breed.setSpecies(request.getSpecies());
            pet.setBreed(breed);
            logger.info("Raza establecida para la mascota: " + request.getBreedName());
        }

        // Convertir la fecha de nacimiento a String si existe
        if (request.getBirthdate() != null) {
            String birthdateString = request.getBirthdate().format(DATE_FORMATTER);
            pet.setBirthdateStr(birthdateString);
            logger.info("Fecha de nacimiento establecida: " + birthdateString);
        }

        pet.setGender(request.getGender());
        pet.setPhotoUrl(request.getPhotoUrl());

        // Establecer la fecha de creación como String
        String createdAtString = LocalDateTime.now().format(DATETIME_FORMATTER);
        pet.setCreatedAtStr(createdAtString);
        logger.info("Fecha de creación establecida: " + createdAtString);

        // Guardar solo el ID del usuario en lugar del objeto completo
        pet.setUserId(user.getId());
        pet.setOwnerName(user.getName()); // Usamos getName() en vez de getFullName() que no existe
        logger.info("Referencia al usuario establecida por ID: " + user.getId());

        // Inicializar listas
        pet.setVaccinations(new ArrayList<>());
        pet.setReminders(new ArrayList<>());
        pet.setAppointments(new ArrayList<>());
        pet.setMedications(new ArrayList<>());

        logger.info("Guardando mascota en Firestore para el usuario: " + user.getId());

        try {
            // Guardar la mascota en Firestore
            String petDocumentId = petRepository.savePet(pet);
            logger.info("Mascota guardada con éxito. Document ID: " + petDocumentId + ", Numeric ID: " + pet.getId());

            // Actualizar la lista de mascotas del usuario
            if (user.getPets() == null) {
                user.setPets(new ArrayList<>());
            }

            // Crear una versión simplificada de la mascota para guardar en el usuario
            Pet simplePet = new Pet();
            simplePet.setId(pet.getId());
            simplePet.setName(pet.getName());
            simplePet.setSpecies(pet.getSpecies());

            // Agregar la mascota a la lista del usuario
            logger.info(
                    "Agregando mascota ID: " + pet.getId() + " a la lista de mascotas del usuario: " + user.getId());
            user.getPets().add(simplePet);

            // Actualizar el documento del usuario en Firestore
            userRepository.updateUser(user);
            logger.info("Usuario actualizado con la nueva mascota: " + user.getId());

            // Crear y devolver la respuesta
            PetResponse response = new PetResponse();
            response.setId(pet.getId());
            response.setName(pet.getName());
            response.setSpecies(pet.getSpecies());

            if (pet.getBreed() != null) {
                response.setBreedName(pet.getBreed().getName());
            }

            // Convertir el String de fecha de nacimiento a LocalDate para la respuesta
            if (pet.getBirthdateStr() != null && !pet.getBirthdateStr().isEmpty()) {
                try {
                    LocalDate birthdate = LocalDate.parse(pet.getBirthdateStr(), DATE_FORMATTER);
                    response.setBirthdate(birthdate);
                } catch (Exception e) {
                    logger.warning("Error al parsear la fecha de nacimiento: " + pet.getBirthdateStr());
                }
            }

            response.setGender(pet.getGender());
            response.setPhotoUrl(pet.getPhotoUrl());
            response.setCreatedAt(pet.getCreatedAtStr());
            response.setUserId(pet.getUserId());

            logger.info("Respuesta de creación de mascota generada con ID: " + response.getId());

            return response;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al guardar mascota en Firestore", e);
            throw e; // Re-lanzar la excepción para que sea manejada en el controlador
        }
    }

    @Override
    public List<PetResponse> getPetsByUser(String userEmail) {
        logger.info("Buscando mascotas para el usuario con email: " + userEmail);

        // Buscar el usuario por email
        Optional<User> userOpt = userRepository.findUserByEmail(userEmail);
        if (userOpt.isEmpty()) {
            logger.warning("No se encontró el usuario con email: " + userEmail);
            throw new IllegalArgumentException("Usuario no encontrado para el email: " + userEmail);
        }

        User user = userOpt.get();
        logger.info("Usuario encontrado con ID: " + user.getId() + ". Buscando sus mascotas...");

        try {
            // Obtener mascotas por userId
            List<Pet> pets = petRepository.findPetsByUserId(user.getId());
            logger.info("Se encontraron " + pets.size() + " mascotas para el usuario " + user.getId());

            // Mapear a la respuesta
            List<PetResponse> responses = pets.stream()
                    .map(pet -> {
                        PetResponse response = new PetResponse();
                        response.setId(pet.getId());
                        response.setName(pet.getName());
                        response.setSpecies(pet.getSpecies());

                        if (pet.getBreed() != null) {
                            response.setBreedName(pet.getBreed().getName());
                        }

                        // Convertir el String de fecha de nacimiento a LocalDate para la respuesta
                        if (pet.getBirthdateStr() != null && !pet.getBirthdateStr().isEmpty()) {
                            try {
                                LocalDate birthdate = LocalDate.parse(pet.getBirthdateStr(), DATE_FORMATTER);
                                response.setBirthdate(birthdate);
                            } catch (Exception e) {
                                logger.warning("Error al parsear la fecha de nacimiento: " + pet.getBirthdateStr());
                            }
                        }

                        response.setGender(pet.getGender());
                        response.setPhotoUrl(pet.getPhotoUrl());
                        response.setCreatedAt(pet.getCreatedAtStr());
                        response.setUserId(pet.getUserId());

                        return response;
                    })
                    .collect(Collectors.toList());

            logger.info("Respuestas de mascotas generadas correctamente");
            return responses;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al buscar mascotas por ID de usuario", e);
            throw e;
        }
    }

    @Override
    public PetResponse getPetById(Long petId, String userEmail) {
        logger.info("Buscando mascota con ID: " + petId + " para usuario: " + userEmail);

        if (petId == null) {
            logger.warning("ID de mascota proporcionado es nulo");
            throw new IllegalArgumentException("El ID de la mascota no puede ser nulo.");
        }

        // Primero obtenemos el usuario actual
        logger.info("Verificando usuario: " + userEmail);
        Optional<User> userOpt = userRepository.findUserByEmail(userEmail);
        if (userOpt.isEmpty()) {
            logger.warning("No se encontró el usuario con email: " + userEmail);
            throw new IllegalArgumentException("Usuario no encontrado para el email: " + userEmail);
        }

        User currentUser = userOpt.get();
        logger.info("Usuario verificado con ID: " + currentUser.getId());

        try {
            // Buscar la mascota por ID
            logger.info("Buscando mascota con ID: " + petId);
            Optional<Pet> petOpt = petRepository.findPetById(petId.toString());

            if (petOpt.isEmpty()) {
                logger.warning("No se encontró mascota con ID: " + petId);
                throw new IllegalArgumentException("Mascota no encontrada con ID: " + petId);
            }

            Pet pet = petOpt.get();
            logger.info("Mascota encontrada: " + pet.getName());

            // Verificar que la mascota pertenece al usuario actual
            if (pet.getUserId() == null) {
                logger.warning("La mascota no tiene usuario asociado. ID mascota: " + petId);
                throw new AccessDeniedException("No tienes permiso para acceder a esta mascota.");
            }

            if (!pet.getUserId().equals(currentUser.getId())) {
                logger.warning("Intento de acceso no autorizado. Usuario: " + currentUser.getId() +
                        " intentó acceder a mascota que pertenece a usuario: " + pet.getUserId());
                throw new AccessDeniedException("No tienes permiso para acceder a esta mascota.");
            }

            logger.info("Acceso verificado. Generando respuesta para mascota: " + pet.getId());

            // Mapear a la respuesta
            PetResponse response = new PetResponse();
            response.setId(pet.getId());
            response.setName(pet.getName());
            response.setSpecies(pet.getSpecies());

            if (pet.getBreed() != null) {
                response.setBreedName(pet.getBreed().getName());
            }

            // Convertir el String de fecha de nacimiento a LocalDate para la respuesta
            if (pet.getBirthdateStr() != null && !pet.getBirthdateStr().isEmpty()) {
                try {
                    LocalDate birthdate = LocalDate.parse(pet.getBirthdateStr(), DATE_FORMATTER);
                    response.setBirthdate(birthdate);
                } catch (Exception e) {
                    logger.warning("Error al parsear la fecha de nacimiento: " + pet.getBirthdateStr());
                }
            }

            response.setGender(pet.getGender());
            response.setPhotoUrl(pet.getPhotoUrl());
            response.setCreatedAt(pet.getCreatedAtStr());
            response.setUserId(pet.getUserId());

            return response;

        } catch (IllegalArgumentException | AccessDeniedException e) {
            // Re-lanza estas excepciones para que sean manejadas apropiadamente
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al buscar mascota", e);
            throw new RuntimeException("Error al buscar mascota: " + e.getMessage(), e);
        }
    }
}