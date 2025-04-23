package cl.pets.patitaspet.user.service;

import cl.pets.patitaspet.user.dto.UserLoginRequest;
import cl.pets.patitaspet.user.dto.UserLoginResponse;
import cl.pets.patitaspet.user.dto.UserRegisterRequest;

public interface UserService {
    void registerUser(UserRegisterRequest request);

    UserLoginResponse loginUser(UserLoginRequest request);
}