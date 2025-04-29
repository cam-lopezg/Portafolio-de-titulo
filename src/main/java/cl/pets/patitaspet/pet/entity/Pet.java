package cl.pets.patitaspet.pet.entity;

import lombok.Getter;
import lombok.Setter;
import cl.pets.patitaspet.appointment.entity.PetAppointment;
import cl.pets.patitaspet.appointment.entity.Reminder;

import java.util.List;


public class Pet {
    private Long id;
    // Se enlaza solo el ID del dueño para optimizar la bdd
    private Long userId;
    // Opcional: podemos mantener algunos datos básicos para mostrar sin necesidad
    // de consultas adicionales
    private String ownerName; // Esto es solo para mostrar información básica en la bdd, no se usa en la app

    private String name;
    private Species species;
    private Breed breed;

    // Usar solo String para fechas - esto elimina el problema de serialización con
    // Firestore
    private String birthdateStr;
    private Gender gender;
    private String photoUrl;
    private String createdAtStr;

    private List<PetVaccination> vaccinations;
    private List<Reminder> reminders;
    private List<PetAppointment> appointments;
    private List<PetMedication> medications;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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

    public Breed getBreed() {
        return breed;
    }

    public void setBreed(Breed breed) {
        this.breed = breed;
    }

    public String getBirthdateStr() {
        return birthdateStr;
    }

    public void setBirthdateStr(String birthdateStr) {
        this.birthdateStr = birthdateStr;
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

    public String getCreatedAtStr() {
        return createdAtStr;
    }

    public void setCreatedAtStr(String createdAtStr) {
        this.createdAtStr = createdAtStr;
    }

    public List<PetVaccination> getVaccinations() {
        return vaccinations;
    }

    public void setVaccinations(List<PetVaccination> vaccinations) {
        this.vaccinations = vaccinations;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
    }

    public List<PetAppointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<PetAppointment> appointments) {
        this.appointments = appointments;
    }

    public List<PetMedication> getMedications() {
        return medications;
    }

    public void setMedications(List<PetMedication> medications) {
        this.medications = medications;
    }
}