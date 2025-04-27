package cl.pets.patitaspet.user.entity;

import lombok.Getter;
import lombok.Setter;
import cl.pets.patitaspet.pet.entity.Pet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Setter
@Getter
public class User {
    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private String photoUrl; // Campo agregado para almacenar la URL de la imagen del usuario
    private String createdAt; // Cambie de LocalDateTime a String para mejor compatibilidad con Firestore, si
                              // no daba error al guardar los usuarios
    private List<Pet> pets;

    // Este constructor sin argumentos necesario para Firestore
    public User() {
    }

    /**
     * Método auxiliar para establecer createdAt a partir de un LocalDateTime
     * Convierte el LocalDateTime a un formato de String más amigable y compacto
     * para la bdd, asi evitamos errores de formato y que los datos del usuario sean
     * demasiado extensos
     * 
     * @param dateTime La fecha y hora a establecer
     */
    public void setCreatedAtFromDateTime(LocalDateTime dateTime) {
        if (dateTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            this.createdAt = dateTime.format(formatter);
        }
    }
}