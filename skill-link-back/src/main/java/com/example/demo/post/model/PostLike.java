package com.example.demo.post.model;

import com.example.demo.user.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "post_likes",
uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "usuario_id"}))
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @Column(name = "fecha_like", columnDefinition = "datetime")
    @CreationTimestamp
    private OffsetDateTime likeDate;

    // Constructors
    public PostLike() {}

    public PostLike(Post post, User user) {
        this.post = post;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OffsetDateTime getLikeDate() {
        return likeDate;
    }

    public void setLikeDate(OffsetDateTime likeDate) {
        this.likeDate = likeDate;
    }
}
