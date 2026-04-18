package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.UserException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for user lifecycle and role management.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Returns active user by identifier. */
    public User getUserById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));
    }

    /** Returns active user by username. */
    public User getUserByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь " + username + " не найден"));
    }

    @Transactional
    /** Creates new active user with USER role. */
    public User createUser(String username, String password) {
        if (userRepository.existsByUsernameAndDeletedFalse(username)) {
            throw new UserException("Пользователь с таким именем уже существует!");
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(Role.USER);
        newUser.setDeleted(false);
        return userRepository.save(newUser);
    }

    /** Returns all active users. */
    public List<User> getAllUsers() {
        return userRepository.findAllByDeletedFalse();
    }

    @Transactional
    /** Updates role for active user. */
    public User updateUserRole(Long userId, Role role) {
        User user = getUserById(userId);
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    /** Performs soft delete for user. */
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        user.setDeleted(true);
        userRepository.save(user);
    }

}