package com.zycus.pims.auth.service;

import com.zycus.pims.auth.dto.*;
import com.zycus.pims.auth.entity.Role;
import com.zycus.pims.auth.entity.User;
import com.zycus.pims.auth.repository.RoleRepository;
import com.zycus.pims.auth.repository.UserRepository;
import com.zycus.pims.common.dto.UserSummaryDto;
import com.zycus.pims.common.enums.RoleName;
import com.zycus.pims.common.exception.DuplicateResourceException;
import com.zycus.pims.common.exception.ResourceNotFoundException;
import com.zycus.pims.common.exception.UnauthorizedException;
import com.zycus.pims.common.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already in use");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getDepartment()
        );

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName(RoleName.EMPLOYEE)
                    .orElseGet(() -> roleRepository.save(new Role(RoleName.EMPLOYEE, "Standard Employee")));
            roles.add(defaultRole);
        } else {
            for (RoleName roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseGet(() -> roleRepository.save(new Role(roleName, roleName.name())));
                roles.add(role);
            }
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        Set<String> roleStrings = savedUser.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        String token = jwtTokenProvider.generateToken(savedUser.getUsername(), savedUser.getId(), roleStrings);
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getUsername());

        return new AuthResponse(
                token,
                refreshToken,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                roleStrings
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is inactive. Please contact your administrator.");
        }

        Set<String> roleStrings = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), roleStrings);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        return new AuthResponse(
                token,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roleStrings
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String username = jwtTokenProvider.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!user.isActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        Set<String> roleStrings = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        String newToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), roleStrings);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        return new AuthResponse(
                newToken,
                newRefreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roleStrings
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Set<String> roleStrings = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDepartment(),
                user.isActive(),
                roleStrings,
                user.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSummaryDto assignRoles(Long userId, Set<RoleName> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = new HashSet<>();
        for (RoleName roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(roleName, roleName.name())));
            roles.add(role);
        }

        user.setRoles(roles);
        User updated = userRepository.save(user);
        return mapToUserSummaryDto(updated);
    }

    private UserSummaryDto mapToUserSummaryDto(User user) {
        Set<String> roleStrings = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDepartment(),
                roleStrings,
                user.isActive()
        );
    }
}
