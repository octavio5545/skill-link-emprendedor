package com.example.demo.user.repository;

import com.example.demo.common.Role;
import com.example.demo.common.UserInterest;
import com.example.demo.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<User> findByActiveTrue(Pageable pageable);
    Optional<User> findByIdAndActiveTrue(Long id);
    List<User> findByActiveTrue();

    List<User> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    @Query("SELECT u FROM User u JOIN u.interests i WHERE i = :interest AND u.active = true")
    List<User> findByInterestContainingAndActiveTrue(@Param("interest")UserInterest interest);

    @Query("SELECT DISTINCT u FROM User u JOIN u.interests i WHERE i IN :interests AND u.active = true")
    List<User> findByInterestsIn(@Param("interests") List<UserInterest> interests);

    List<User> findByRoleAndActiveTrue(Role role);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.interests i WHERE i = :interest AND u.active = true")
    long countByInterest(@Param("interest") UserInterest interest);

}