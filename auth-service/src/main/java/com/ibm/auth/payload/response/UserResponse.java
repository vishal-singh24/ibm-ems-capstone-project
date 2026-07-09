package com.ibm.auth.payload.response;

import com.ibm.auth.payload.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {

    private String id;

    private String username;

    private String email;

    private Set<Role> roles;

    private boolean enabled;

    private boolean deleted;

}
