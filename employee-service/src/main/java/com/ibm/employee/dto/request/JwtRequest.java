package com.ibm.employee.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class JwtRequest {

    private String username;

    private List<String> roles;
}