package com.example.bankcards.controller;
import com.example.bankcards.dto.UserResponseDto;
import com.example.bankcards.dto.UserRegistrationDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller for user registration and admin user operations.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "Методы для управления клиентами банка")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Registers new user in the system. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового клиента в базе данных. Логин должен быть уникальным.")
    public String registerUser(@Valid @RequestBody UserRegistrationDto request) {
        User savedUser = userService.createUser(request.getUsername(), request.getPassword());
        return "Пользователь " + savedUser.getUsername() + " успешно зарегистрирован с ID: " + savedUser.getId();
    }

    /** Returns all active users for admin. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить всех пользователей", description = "Доступно только администратору.")
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getRole()))
                .toList();
    }

    /** Updates user role for admin. */
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Изменить роль пользователя", description = "Доступно только администратору.")
    public UserResponseDto updateUserRole(@PathVariable Long userId, @RequestParam Role role) {
        User updatedUser = userService.updateUserRole(userId, role);
        return new UserResponseDto(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getRole());
    }

    /** Performs soft delete of user for admin. */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить пользователя", description = "Доступно только администратору.")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }
}
