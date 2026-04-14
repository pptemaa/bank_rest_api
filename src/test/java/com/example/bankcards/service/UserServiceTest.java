package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        when(userRepository.findByUsername("new_user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser("new_user", "secret123");

        assertEquals("new_user", created.getUsername());
        assertEquals("encoded_secret", created.getPassword());
        assertEquals(Role.USER, created.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUserDuplicate() {
        User existing = new User();
        existing.setUsername("existing");
        when(userRepository.findByUsername("existing")).thenReturn(Optional.of(existing));

        assertThrows(UserException.class, () -> userService.createUser("existing", "123456"));
        verify(userRepository, never()).save(any(User.class));
    }
}
