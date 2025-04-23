package cl.pets.patitaspet.pet.dto;

import cl.pets.patitaspet.pet.entity.Gender;
import cl.pets.patitaspet.pet.entity.Species;

import java.time.LocalDate;

public class PetCreateRequest {
    private String name;
    private Species species;
    private String breedName;
    private LocalDate birthdate;
    private Gender gender;
    private String photoUrl;

    public PetCreateRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public String getBreedName() {
        return breedName;
    }

    public void setBreedName(String breedName) {
        this.breedName = breedName;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}