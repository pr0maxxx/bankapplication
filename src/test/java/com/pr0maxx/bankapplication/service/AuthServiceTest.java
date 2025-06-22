package com.pr0maxx.bankapplication.service;

import com.pr0maxx.bankapplication.dto.RegisterRequest;
import com.pr0maxx.bankapplication.model.User;
import com.pr0maxx.bankapplication.model.enums.Role;
import com.pr0maxx.bankapplication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_NewUser_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        request.setEmail("user@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setUsername("newuser");
        savedUser.setPassword("encodedPassword");
        savedUser.setEmail("user@example.com");
        savedUser.setRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(request);

        assertEquals("newuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals("user@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_UserAlreadyExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.register(request));

        assertEquals("Пользователь уже существует", ex.getMessage());
    }

    @Test
    void testFindByUsername_UserExists_ReturnsUser() {
        User user = new User();
        user.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = authService.findByUsername("testuser");

        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testFindByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.findByUsername("notfound"));

        assertEquals("Пользователь не найден", ex.getMessage());
    }
}
