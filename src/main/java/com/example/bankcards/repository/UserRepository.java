package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for user persistence operations.
 */
public interface UserRepository extends JpaRepository<User,Long> {
    /** Finds active user by username. */
    Optional<User> findByUsernameAndDeletedFalse(String userName);

    /** Checks active user existence by username. */
    boolean existsByUsernameAndDeletedFalse(String userName);

    /** Finds active user by id. */
    Optional<User> findByIdAndDeletedFalse(Long id);

    /** Returns all active users. */
    List<User> findAllByDeletedFalse();
}
