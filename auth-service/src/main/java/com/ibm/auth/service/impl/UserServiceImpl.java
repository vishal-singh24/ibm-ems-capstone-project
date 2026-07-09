package com.ibm.auth.service.impl;

import com.ibm.auth.common.exception.EmailAlreadyExistsException;
import com.ibm.auth.common.exception.UserNotFoundException;
import com.ibm.auth.common.exception.UsernameAlreadyExistsException;
import com.ibm.auth.common.payload.ApiResponse;
import com.ibm.auth.entity.User;
import com.ibm.auth.payload.enums.Role;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;

        /**
         * Entity -> DTO
         */
        private UserResponse mapToResponse(User user) {

                return UserResponse.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .roles(user.getRoles())
                                .enabled(user.isEnabled())
                                .deleted(user.isDeleted())
                                .build();
        }

        /**
         * Get All Users
         */
        @Override
        public ApiResponse<List<UserResponse>> getAllUsers() {

                List<UserResponse> users = userRepository.findAll()
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
                                
                return new ApiResponse<>(
                                true,
                                "Users fetched successfully",
                                users);
        }

        /**
         * Get Logged-in User
         */
        @Override
        public ApiResponse<UserResponse> getCurrentUser() {

                User user = getLoggedInUser();

                return new ApiResponse<>(
                                true,
                                "User fetched successfully",
                                mapToResponse(user));
        }

        /**
         * Get User By Id
         */
        @Override
        public ApiResponse<UserResponse> getUserById(String id) {

                User loggedInUser = getLoggedInUser();

                User targetUser = userRepository.findByIdAndDeletedFalse(id)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                if (!isAdmin(loggedInUser)
                                && !loggedInUser.getId().equals(id)) {

                        throw new RuntimeException("Access Denied");
                }

                return new ApiResponse<>(
                                true,
                                "User fetched successfully",
                                mapToResponse(targetUser));
        }

        /**
         * Update User
         */
        @Override
        public ApiResponse<UserResponse> updateUser(
                        String id,
                        UpdateUserRequest request) {

                User loggedInUser = getLoggedInUser();

                User user = userRepository.findByIdAndDeletedFalse(id)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                if (!isAdmin(loggedInUser)
                                && !loggedInUser.getId().equals(id)) {

                        throw new RuntimeException("Access Denied");
                }

                // Username validation
                if (request.getUsername() != null
                                && !request.getUsername().equals(user.getUsername())) {

                        if (userRepository.existsByUsername(request.getUsername())) {
                                throw new UsernameAlreadyExistsException(
                                                "Username already exists");
                        }

                        user.setUsername(request.getUsername());
                }

                // Email validation
                if (request.getEmail() != null
                                && !request.getEmail().equals(user.getEmail())) {

                        if (userRepository.existsByEmail(request.getEmail())) {
                                throw new EmailAlreadyExistsException(
                                                "Email already exists");
                        }

                        user.setEmail(request.getEmail());
                }

                user.setUpdatedAt(LocalDateTime.now());

                User savedUser = userRepository.save(user);

                return new ApiResponse<>(
                                true,
                                "User updated successfully",
                                mapToResponse(savedUser));
        }

        @Override
        public ApiResponse<Void> deleteUser(String id) {

                User loggedInUser = getLoggedInUser();

                if (!isAdmin(loggedInUser)) {
                        throw new RuntimeException("Access Denied");
                }

                User user = userRepository.findByIdAndDeletedFalse(id)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                // Soft delete
                user.setDeleted(true);
                user.setEnabled(false);
                user.setUpdatedAt(LocalDateTime.now());

                userRepository.save(user);

                return new ApiResponse<>(
                                true,
                                "User deleted successfully",
                                null);
        }

        @Override
        public ApiResponse<Void> assignRoles(String id, Set<Role> roles) {

                User loggedInUser = getLoggedInUser();

                if (!isAdmin(loggedInUser)) {
                        throw new RuntimeException("Access Denied");
                }

                User user = userRepository.findByIdAndDeletedFalse(id)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                user.setRoles(roles);
                user.setUpdatedAt(LocalDateTime.now());

                userRepository.save(user);

                return new ApiResponse<>(
                                true,
                                "Roles assigned successfully",
                                null);
        }

        @Override
        public ApiResponse<Void> enableUser(String id) {

                User loggedInUser = getLoggedInUser();

                if (!isAdmin(loggedInUser)) {
                        throw new RuntimeException("Access Denied");
                }

                User user = userRepository.findByIdAndDeletedFalse(id)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                user.setEnabled(true);
                user.setUpdatedAt(LocalDateTime.now());

                userRepository.save(user);

                return new ApiResponse<>(
                                true,
                                "User enabled successfully",
                                null);
        }

        @Override
        public ApiResponse<Void> disableUser(String id) {

                User loggedInUser = getLoggedInUser();

                if (!isAdmin(loggedInUser)) {
                        throw new RuntimeException("Access Denied");
                }

                User user = userRepository.findByIdAndDeletedFalse(id)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                user.setEnabled(false);
                user.setUpdatedAt(LocalDateTime.now());

                userRepository.save(user);

                return new ApiResponse<>(
                                true,
                                "User disabled successfully",
                                null);
        }

        /**
         * Logged-in User
         */
        private User getLoggedInUser() {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                String username = authentication.getName();

                return userRepository.findByUsernameAndDeletedFalse(username)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));
        }

        /**
         * Check ADMIN Role
         */
        private boolean isAdmin(User user) {

                return user.getRoles()
                                .contains(Role.ROLE_ADMIN);
        }

}