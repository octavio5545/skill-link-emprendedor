package com.example.demo.postInteractions.repository;

import com.example.demo.postInteractions.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Método existente sin cambios
    List<Post> findByUser_Id(Long userId);

    // Nuevo método para paginación por usuario
    Page<Post> findByUser_IdOrderByFechaPublicacionDesc(Long userId, Pageable pageable);

    // Nuevo método para obtener posts paginados ordenados por fecha
    Page<Post> findAllByOrderByFechaPublicacionDesc(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id IN :ids ORDER BY p.fechaPublicacion DESC")
    List<Post> findAllByIdWithTags(@Param("ids") List<Long> ids);

    boolean existsById(Long id);
}