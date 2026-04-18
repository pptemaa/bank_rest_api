package com.example.bankcards.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User entity representing a bank system account.
 */
@Table(name = "users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,unique = true)
    private String username;
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Card> cards;

    public User() {
    }


    public Long getId() { return id; }

    /** Returns username for authentication. */
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /** Returns encoded password hash. */
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /** Returns current role in the system. */
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    /** Returns soft-delete flag. */
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    /** Returns creation timestamp. */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** Returns last update timestamp. */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
