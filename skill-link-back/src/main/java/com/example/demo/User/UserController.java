package com.example.demo.User;

import com.example.demo.User.dto.UserRegisterRequest;
import com.example.demo.User.dto.UserResponse;
import com.example.demo.infra.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequest userRegisterRequest){
        try{
            System.out.println("Hola soy Andy");
            if(userRegisterRequest.role().equals("ADMIN")) {
                return ResponseEntity.badRequest().body("No se puede registrar un usuario con el rol ADMIN directamente. Use el endpoint de administración.");
            }
            User user = userService.register(userRegisterRequest);
            UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getSecondName(), user.getEmail(), user.getRole());
            String jwtToken = tokenService.generateToken(user);
            Map<String, Object> response = Map.of(
                "token", jwtToken,
                "user", userResponse
            );
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar el usuario: " + e.getMessage());
        }
    }

}
