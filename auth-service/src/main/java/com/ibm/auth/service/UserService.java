package com.ibm.auth.service;

import com.ibm.auth.common.payload.ApiResponse;
import com.ibm.auth.payload.enums.Role;
import com.ibm.auth.payload.request.UpdateUserRequest;
import com.ibm.auth.payload.response.SearchResponse;
import com.ibm.auth.payload.response.UserResponse;

import java.util.List;
import java.util.Set;

public interface UserService {

    ApiResponse<List<UserResponse>> getAllUsers();

    ApiResponse<UserResponse> getCurrentUser();

    ApiResponse<UserResponse> getUserById(String id);

    ApiResponse<UserResponse> updateUser(String id,
            UpdateUserRequest request);
    
    ApiResponse<Void> deleteUser(String id);

    ApiResponse<Void> assignRoles(String id, Set<Role> roles);

    ApiResponse<Void> enableUser(String id);

    ApiResponse<Void> disableUser(String id);

    ApiResponse<SearchResponse> getUserByEmployeeId(String employeeId);


}