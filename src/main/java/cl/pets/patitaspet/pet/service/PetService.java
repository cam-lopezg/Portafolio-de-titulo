package cl.pets.patitaspet.pet.service;

import cl.pets.patitaspet.pet.dto.PetCreateRequest;
import cl.pets.patitaspet.pet.dto.PetResponse;

import java.util.List;

public interface PetService {
    PetResponse createPet(PetCreateRequest request, String userEmail);

    List<PetResponse> getPetsByUser(String userEmail);

    PetResponse getPetById(Long petId, String userEmail);
}