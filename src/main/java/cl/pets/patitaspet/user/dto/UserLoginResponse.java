package cl.pets.patitaspet.user.dto;

public class UserLoginResponse {
    private Long id;
    private String name;
    private String email;
    private String token;
    private boolean success;

    public UserLoginResponse() {
    }

    public UserLoginResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.success = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}