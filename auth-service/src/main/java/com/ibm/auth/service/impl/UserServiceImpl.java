package com.ibm.auth.service.impl;

import com.ibm.auth.common.exception.EmailAlreadyExistsException;
import com.ibm.auth.common.exception.UserNotFoundException;
import com.ibm.auth.common.exception.UsernameAlreadyExistsException;
import com.ibm.auth.entity.User;
import com.ibm.auth.payload.request.UpdateUserRequest;
import com.ibm.auth.payload.response.UserResponse;
import com.ibm.auth.repository.UserRepository;
import com.ibm.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Convert Entity -> DTO
     */
    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .build();
    }

    /**
     * Get All Users
     */
    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Logged In User
     */
    @Override
    public UserResponse getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return mapToResponse(user);
    }

        /**
     * Get User By Id
     */
    @Override
    public UserResponse getUserById(String id) {

        User loggedInUser = getLoggedInUser();

        User targetUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (!isAdmin(loggedInUser)
                && !loggedInUser.getId().equals(id)) {

            throw new RuntimeException("Access Denied");
        }

        return mapToResponse(targetUser);
    }

    /**
     * Update User
     */
    @Override
    public UserResponse updateUser(String id,
                                   UpdateUserRequest request) {

        User loggedInUser = getLoggedInUser();

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        // Only ADMIN or owner
        if (!isAdmin(loggedInUser)
                && !loggedInUser.getId().equals(id)) {

            throw new RuntimeException("Access Denied");
        }

        // Username validation
        if (!user.getUsername().equals(request.getUsername())) {

            userRepository.findByUsername(request.getUsername())
                    .ifPresent(u -> {
                        throw new UsernameAlreadyExistsException(
                                "Username already exists");
                    });

            user.setUsername(request.getUsername());
        }

        // Email validation
        if (!user.getEmail().equals(request.getEmail())) {

            userRepository.findByEmail(request.getEmail())
                    .ifPresent(u -> {
                        throw new EmailAlreadyExistsException(
                                "Email already exists");
                    });

            user.setEmail(request.getEmail());
        }

        // Update fields
        user.setEnabled(request.isEnabled());

        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    /**
     * Get Logged-in User
     */
    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
    }

    /**
     * Check ADMIN Role
     */
    private boolean isAdmin(User user) {

        return user.getRoles()
                .stream()
                .anyMatch(role ->
                        role.name().equals("ROLE_ADMIN"));
    }


}