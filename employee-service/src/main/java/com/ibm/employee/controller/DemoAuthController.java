package com.ibm.employee.controller;

import com.ibm.employee.dto.request.JwtRequest;
import com.ibm.employee.dto.response.JwtResponse;
import com.ibm.employee.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class DemoAuthController {

    private final JwtService jwtService;

    @PostMapping("/token")
    public JwtResponse generateToken(
            @RequestBody JwtRequest request) {

        String token = jwtService.generateToken(
                request.getUsername(),
                request.getRoles());

        return new JwtResponse(token);
    }
}