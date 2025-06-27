package com.example.demo.user.repository;

import com.example.demo.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    UserDetails findByEmail(String email);
    Optional<User> findUserByEmail(String email);
    boolean existsByEmail(String email);

    // Consulta optimizada con JOIN FETCH para evitar N+1
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.interests WHERE u.id IN :ids")
    List<User> findAllByIdWithInterests(@Param("ids") List<Long> ids);

    boolean existsById(Long id);
}