package com.example.bankcards.controller;
import com.example.bankcards.dto.UserRegistrationDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "Методы для управления клиентами банка")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового клиента в базе данных. Логин должен быть уникальным.")
    public String registerUser(@Valid @RequestBody UserRegistrationDto request) {
        User savedUser = userService.createUser(request.getUsername(), request.getPassword());
        return "Пользователь " + savedUser.getUsername() + " успешно зарегистрирован с ID: " + savedUser.getId();
    }
}
