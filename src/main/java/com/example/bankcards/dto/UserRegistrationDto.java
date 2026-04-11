package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Имя пользователя обязательно")
    private String username;
    @NotBlank(message = "Пароль пользователя обязательно")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;
}