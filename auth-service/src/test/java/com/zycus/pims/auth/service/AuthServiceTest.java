package com.zycus.pims.auth.service;

import com.zycus.pims.auth.dto.AuthResponse;
import com.zycus.pims.auth.dto.LoginRequest;
import com.zycus.pims.auth.dto.RegisterRequest;
import com.zycus.pims.auth.entity.Role;
import com.zycus.pims.auth.entity.User;
import com.zycus.pims.auth.repository.RoleRepository;
import com.zycus.pims.auth.repository.UserRepository;
import com.zycus.pims.common.enums.RoleName;
import com.zycus.pims.common.exception.DuplicateResourceException;
import com.zycus.pims.common.exception.UnauthorizedException;
import com.zycus.pims.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest(
                "newuser", "pass123", "newuser@example.com", "John", "Doe", "Engineering", Set.of(RoleName.EMPLOYEE)
        );

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleName.EMPLOYEE)).thenReturn(Optional.of(new Role(RoleName.EMPLOYEE, "Employee")));

        User savedUser = new User("newuser", "encodedPassword", "newuser@example.com", "John", "Doe", "Engineering");
        savedUser.setId(10L);
        savedUser.setRoles(Set.of(new Role(RoleName.EMPLOYEE, "Employee")));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtTokenProvider.generateToken(eq("newuser"), eq(10L), any())).thenReturn("mockJwtToken");
        when(jwtTokenProvider.generateRefreshToken("newuser")).thenReturn("mockRefreshToken");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("mockJwtToken");
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRoles()).contains("EMPLOYEE");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowWhenRegisteringDuplicateUsername() {
        RegisterRequest request = new RegisterRequest(
                "existinguser", "pass123", "new@example.com", "John", "Doe", "Engineering", null
        );

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        LoginRequest request = new LoginRequest("testuser", "correctPassword");

        User user = new User("testuser", "hashedPassword", "test@example.com", "Test", "User", "IT");
        user.setId(5L);
        user.setRoles(Set.of(new Role(RoleName.ADMIN, "Admin")));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(eq("testuser"), eq(5L), any())).thenReturn("validToken");
        when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("validRefreshToken");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("validToken");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRoles()).contains("ADMIN");
    }

    @Test
    void shouldThrowUnauthorizedOnInvalidPassword() {
        LoginRequest request = new LoginRequest("testuser", "wrongPassword");

        User user = new User("testuser", "hashedPassword", "test@example.com", "Test", "User", "IT");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid username or password");
    }
}
