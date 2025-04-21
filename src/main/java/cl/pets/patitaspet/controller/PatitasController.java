package cl.pets.patitaspet.controller;

import cl.pets.patitaspet.model.UserRegisterRequest;
import cl.pets.patitaspet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
public class PatitasController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String holaMundo() {
        return "Hola Mundo";
    }

    @PostMapping
    public String holaMundo(@RequestBody String request) {
        return "Peticion: " + request;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody UserRegisterRequest userRegisterRequest) {
        userService.registerUser(userRegisterRequest);
        return "Usuario registrado exitosamente.";
    }
}
