package com.example.bankcards.service;
import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.UserException;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));
    }
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь " + username + " не найден"));
    }

    @Transactional
    public User createUser(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserException("Пользователь с таким именем уже существует!");
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole(Role.USER);
        return userRepository.save(newUser);
    }

}