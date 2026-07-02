package com.ibm.auth.service;

import com.ibm.auth.common.payload.ApiResponse;
import com.ibm.auth.payload.request.UpdateUserRequest;
import com.ibm.auth.payload.response.UserResponse;

import java.util.List;

public interface UserService {

    ApiResponse<List<UserResponse>> getAllUsers();

    ApiResponse<UserResponse> getCurrentUser();

    ApiResponse<UserResponse> getUserById(String id);

    ApiResponse<UserResponse> updateUser(String id,
            UpdateUserRequest request);
}