package com.example.bankcards.controller;
import com.example.bankcards.dto.ErrorResponseDto;
import com.example.bankcards.dto.JwtResponseDto;
import com.example.bankcards.dto.UserLoginDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Авторизация", description = "Выдача цифровых ключей (JWT)")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    @PostMapping
    @Operation(summary = "Войти в систему",description = "Проверяет логин/пароль и выдает JWT токен")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDto request){
        User user = userService.getUserByUsername(request.getUsername());
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDto("Неверный пароль!"));
        }
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new JwtResponseDto(token));
    }
}
