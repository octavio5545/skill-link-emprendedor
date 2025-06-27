package com.example.demo.postInteractions.repository;

import com.example.demo.postInteractions.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Encontrar todos los comentarios de un post específico, ordenados por fecha
    List<Comment> findByPost_IdOrderByFechaComentarioAsc(Long postId);

    // Encontrar comentarios de nivel superior (aquellos sin parentComment) de un post
    List<Comment> findByPost_IdAndParentCommentIsNullOrderByFechaComentarioAsc(Long postId);

    // Encontrar todas las respuestas a un comentario padre específico
    List<Comment> findByParentComment_IdOrderByFechaComentarioAsc(Long parentCommentId);

    @Query("SELECT DISTINCT c FROM Comment c " +
            "LEFT JOIN FETCH c.replies r " +
            "WHERE c.post.id = :postId AND c.parentComment IS NULL " +
            "ORDER BY c.fechaComentario ASC")
    List<Comment> findTopLevelCommentsWithReplies(@Param("postId") Long postId);

    @Query("SELECT DISTINCT c FROM Comment c " +
            "LEFT JOIN FETCH c.replies r " +
            "LEFT JOIN FETCH c.user cu " +
            "LEFT JOIN FETCH cu.interests " +
            "LEFT JOIN FETCH r.user ru " +
            "LEFT JOIN FETCH ru.interests " +
            "WHERE c.post.id = :postId AND c.parentComment IS NULL " +
            "ORDER BY c.fechaComentario ASC")
    List<Comment> findCommentsWithEverything(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c " +
            "WHERE c.post.id IN :postIds " +
            "ORDER BY c.post.id, c.fechaComentario ASC")
    List<Comment> findCommentsWithRepliesByMultiplePostIds(@Param("postIds") List<Long> postIds);

    boolean existsById(Long id);
}