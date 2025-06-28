package com.example.demo.chat.repository;

import com.example.demo.chat.model.Conversacion;
import com.example.demo.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    Optional<Conversacion> findByUsuario1AndUsuario2(User usuario1, User usuario2);
    Optional<Conversacion> findByUsuario2AndUsuario1(User usuario1, User usuario2);

    @Query("SELECT DISTINCT c FROM Conversacion c " +
            "LEFT JOIN FETCH c.usuario1 " +
            "LEFT JOIN FETCH c.usuario2 " +
            "WHERE c.usuario1 = :usuario OR c.usuario2 = :usuario")
    List<Conversacion> findConversacionesConUsuarios(@Param("usuario") User usuario);
}