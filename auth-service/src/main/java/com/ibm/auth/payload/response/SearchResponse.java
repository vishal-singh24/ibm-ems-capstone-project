package com.ibm.auth.payload.response;

import com.ibm.auth.payload.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@Builder
public class SearchResponse {

    private String id;

    private String employeeId;

    private String username;

    private String email;

    private Set<Role> roles;

    private boolean enabled;

    private boolean deleted;
}
