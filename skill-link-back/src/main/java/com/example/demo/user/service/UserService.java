package com.example.demo.user.service;

import com.example.demo.common.UserInterest;
import com.example.demo.user.dto.UserDetailsResponse;
import com.example.demo.user.dto.UserRegisterRequest;
import com.example.demo.user.dto.UserResponse;
import com.example.demo.user.dto.UserUpdateRequest;
import com.example.demo.user.model.User;
import com.example.demo.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(UserRegisterRequest userRegisterRequest) {
        // Verificar si el email ya existe
        if(userRepository.existsByEmail(userRegisterRequest.email())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear usuario
        String hashedPassword = passwordEncoder.encode(userRegisterRequest.password());
        User user = new User(userRegisterRequest);
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    public Page<UserResponse> getAllUsers(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByActiveTrue(pageable)
                .map(this::convertToUserResponse);
    }

    public Optional<UserDetailsResponse> getUserById(Long userId){
        return userRepository.findByIdAndActiveTrue(userId)
                .map(this::convertToUserDetailsResponse);
    }

    @Transactional
    public User updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar campos
        if(userUpdateRequest.name() != null) {
            user.setName(userUpdateRequest.name());
        }
        if(userUpdateRequest.secondName() != null) {
            user.setSecondName(userUpdateRequest.secondName());
        }
        if(userUpdateRequest.email() != null && !userUpdateRequest.email().equals(user.getEmail())) {
            if(userRepository.existsByEmail(userUpdateRequest.email())) {
                throw new RuntimeException("El email ya está registrado");
            }
            user.setEmail(userUpdateRequest.email());
        }
        if(userUpdateRequest.interests() != null) {
            user.setInterests(userUpdateRequest.interests());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setActive(false);
        userRepository.save(user);
    }

    public List<UserResponse> getUsersByInterest(UserInterest interest){
        return userRepository.findByInterestContainingAndActiveTrue(interest)
                .stream()
                .map(this::convertToUserResponse)
                .toList();
    }

    public List<UserResponse> searchUsersByName(String name){
        return userRepository.findByNameContainingIgnoreCaseAndActiveTrue(name)
                .stream()
                .map(this::convertToUserResponse)
                .toList();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDetails user = userRepository.findByEmail(email);
        if(user == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con email: " + email);
        }
        return user;
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getSecondName(),
                user.getEmail(),
                user.getRole(),
                user.getInterests()
        );
    }

    private UserDetailsResponse convertToUserDetailsResponse(User user) {
        return new UserDetailsResponse(
                user.getId(),
                user.getName(),
                user.getSecondName(),
                user.getEmail(),
                user.getRole(),
                user.getInterests(),
                user.getRegistrationDate(),
                user.isActive()
        );
    }

}