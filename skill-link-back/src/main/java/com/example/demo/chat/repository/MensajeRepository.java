package com.example.demo.chat.repository;

import com.example.demo.chat.model.Conversacion;
import com.example.demo.chat.model.Mensaje;
import com.example.demo.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m " +
            "LEFT JOIN FETCH m.emisor " +
            "WHERE m.conversacion = :conversacion " +
            "ORDER BY m.timestampEnvio DESC")
    List<Mensaje> findTop20ByConversacionWithEmisor(@Param("conversacion") Conversacion conversacion);

    @Query("SELECT m FROM Mensaje m " +
            "LEFT JOIN FETCH m.emisor " +
            "WHERE m.conversacion = :conversacion " +
            "ORDER BY m.timestampEnvio DESC")
    Page<Mensaje> findByConversacionWithEmisorOrderByTimestampEnvioDesc(
            @Param("conversacion") Conversacion conversacion,
            Pageable pageable);

    List<Mensaje> findByConversacionAndEmisorNotAndLeidoFalse(Conversacion conversacion, User emisor);

    @Query("SELECT m FROM Mensaje m " +
            "LEFT JOIN FETCH m.emisor " +
            "WHERE m.conversacion IN :conversaciones " +
            "AND m.timestampEnvio = (" +
            "    SELECT MAX(m2.timestampEnvio) " +
            "    FROM Mensaje m2 " +
            "    WHERE m2.conversacion = m.conversacion" +
            ")")
    List<Mensaje> findUltimosMensajesPorConversaciones(@Param("conversaciones") List<Conversacion> conversaciones);

    @Query("SELECT m.conversacion.id, COUNT(m) FROM Mensaje m " +
            "WHERE m.conversacion IN :conversaciones " +
            "AND m.emisor != :usuario " +
            "AND m.leido = false " +
            "GROUP BY m.conversacion.id")
    List<Object[]> countMensajesNoLeidosPorConversacion(
            @Param("conversaciones") List<Conversacion> conversaciones,
            @Param("usuario") User usuario);
}