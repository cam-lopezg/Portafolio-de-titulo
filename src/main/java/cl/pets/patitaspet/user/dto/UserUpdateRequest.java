package cl.pets.patitaspet.user.dto;

public class UserUpdateRequest {
    private String name;
    private String phoneNumber;
    private String address;
    private String birthDate;

    public UserUpdateRequest() {
    }

    public UserUpdateRequest(String name, String phoneNumber, String address, String birthDate) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}