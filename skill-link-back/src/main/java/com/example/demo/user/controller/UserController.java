package com.example.demo.user.controller;

import com.example.demo.common.UserInterest;
import com.example.demo.user.service.UserService;
import com.example.demo.user.dto.*;
import com.example.demo.infra.security.TokenService;
import com.example.demo.user.model.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest){
        try{
            System.out.println("=== REGISTRO DE USUARIO ===");
            System.out.println("Datos recibidos: " + userRegisterRequest);

            // Validar que no sea admin
            if(userRegisterRequest.role().name().equals("Admin")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No se puede registrar un usuario con el rol ADMIN."));
            }

            User user = userService.register(userRegisterRequest);
            System.out.println("Usuario creado con ID: " + user.getId());

            String jwtToken = tokenService.generateToken(user);
            System.out.println("Token generado exitosamente");

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getSecondName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getInterests()
            );

            UserRegisterResponse response = new UserRegisterResponse(jwtToken, userResponse);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Error de runtime: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest){
        try{
            System.out.println("=== LOGIN DE USUARIO ===");
            System.out.println("Email: " + userLoginRequest.email());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userLoginRequest.email(), userLoginRequest.password());

            Authentication authentication = authenticationManager.authenticate(authToken);
            User user = (User) authentication.getPrincipal();

            if (!user.isActive()) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Tu cuenta está desactivada."));
            }

            String jwtToken = tokenService.generateToken(user);
            System.out.println("Login exitoso para usuario: " + user.getEmail());

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getSecondName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getInterests()
            );

            UserLoginResponse response = new UserLoginResponse(
                    jwtToken,
                    userResponse
            );

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            System.err.println("Credenciales incorrectas para: " + userLoginRequest.email());
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Email o contraseña incorrectos."));
        } catch (AuthenticationException e) {
            System.err.println("Error de autenticación: " + e.getMessage());
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Error de autenticación."));
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor."));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        try{
            Page<UserResponse> usersPage = userService.getAllUsers(page, size);
            return ResponseEntity.ok(usersPage);
        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUsersById(@PathVariable Long userId){
        try{
            return userService.getUserById(userId)
                    .map(user -> ResponseEntity.ok(user))
                    .orElse(ResponseEntity.notFound().build());
        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @PutMapping("/")
    public ResponseEntity<?> updateUser(
            @Valid @RequestBody UserUpdateRequest userUpdateRequest){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();

            User updatedUser = userService.updateUser(userId, userUpdateRequest);

            UserDetailsResponse response = new UserDetailsResponse(
                    updatedUser.getId(),
                    updatedUser.getName(),
                    updatedUser.getSecondName(),
                    updatedUser.getEmail(),
                    updatedUser.getRole(),
                    updatedUser.getInterests(),
                    updatedUser.getRegistrationDate(),
                    updatedUser.isActive()
            );

            return ResponseEntity.ok(response);
        }catch (RuntimeException e){
            System.err.println("Error de runtime al actualizar usuario: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor."));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deactivateUserByAdmin(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(Map.of("message", "Usuario desactivado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deactivateUserBySelf(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = ((User) authentication.getPrincipal()).getId();
            userService.deactivateUser(userId);
            return ResponseEntity.ok(Map.of("message", "Usuario desactivado exitosamente"));
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @GetMapping("/interest/{interest}")
    public ResponseEntity<List<UserResponse>> getUsersByInterest(@PathVariable UserInterest interest){
        try{
            List<UserResponse> users = userService.getUsersByInterest(interest);
            return ResponseEntity.ok(users);
        }catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String name){
        try{
            List<UserResponse> users = userService.searchUsersByName(name);
            return ResponseEntity.ok(users);
        }catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }


}