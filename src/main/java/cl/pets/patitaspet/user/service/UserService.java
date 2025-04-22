package cl.pets.patitaspet.user.service;

import cl.pets.patitaspet.user.dto.UserRegisterRequest;

public interface UserService {
    void registerUser(UserRegisterRequest request);
}