package cl.pets.patitaspet.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class User {
    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    private List<Pet> pets;
}
