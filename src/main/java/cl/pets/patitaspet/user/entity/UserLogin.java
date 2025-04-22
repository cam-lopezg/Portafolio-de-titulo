package cl.pets.patitaspet.user.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserLogin {
    private Long id;
    private User user;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private boolean successful;
    private String location;
}