package com.example.demo.postInteractions.repository;

import com.example.demo.postInteractions.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser_Id(Long userId);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id IN :ids")
    List<Post> findAllByIdWithTags(@Param("ids") List<Long> ids);

    boolean existsById(Long id);
}