package cl.pets.patitaspet.pet.service;

import cl.pets.patitaspet.pet.dto.PetCreateRequest;
import cl.pets.patitaspet.pet.dto.PetResponse;
import cl.pets.patitaspet.pet.entity.Pet;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PetService {
    PetResponse createPet(PetCreateRequest request, String userEmail);

    List<PetResponse> getPetsByUser(String userEmail);

    PetResponse getPetById(Long petId, String userEmail);

    // Nuevos métodos para gestión de imágenes de mascotas
    Pet updatePetImage(Long petId, String userEmail, MultipartFile imageFile) throws IOException;

    String getPetImageUrl(Long petId, String userEmail);
}