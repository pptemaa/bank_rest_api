package com.example.bankcards.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем защиту от подделки запросов (CSRF), иначе Postman не сможет отправлять POST и PATCH
                .csrf(AbstractHttpConfigurer::disable)

                // Настраиваем правила доступа
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ВРЕМЕННО разрешаем доступ ко всем адресам без пароля
                );

        return http.build();
    }
}