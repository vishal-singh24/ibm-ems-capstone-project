package com.ibm.auth.controller;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ibm.auth.common.exception.PasswordRecoveryException;
import com.ibm.auth.payload.request.PasswordDtos.ApiResponse;
import com.ibm.auth.payload.request.PasswordDtos.ChangePasswordRequest;
import com.ibm.auth.payload.request.PasswordDtos.ForgotPasswordRequest;
import com.ibm.auth.payload.request.PasswordDtos.ResetPasswordRequest;
import com.ibm.auth.payload.request.PasswordDtos.VerifyOtpRequest;
import com.ibm.auth.payload.request.PasswordDtos.VerifyOtpResponse;
import com.ibm.auth.entity.User;
import com.ibm.auth.repository.UserRepository;
import com.ibm.auth.service.EmailService;
import com.ibm.auth.service.OtpService;

@RestController
@RequestMapping("/api/v1/auth")
public class EmailController {

    private final OtpService otpService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmailController(OtpService otpService,
                           EmailService emailService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.otpService = otpService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

 
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user ->
                otpService.generateAndSendOtp(request.getEmail())
        );

        return ResponseEntity.ok(new ApiResponse(true,
                "If an account exists with this email, an OTP has been sent"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            String resetToken = otpService.verifyOtp(request.getEmail(), request.getOtp());
            return ResponseEntity.ok(new VerifyOtpResponse(true, "OTP verified successfully", resetToken));
        } catch (PasswordRecoveryException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new VerifyOtpResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            otpService.validateResetToken(request.getEmail(), request.getResetToken());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new PasswordRecoveryException("Account not found"));

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            otpService.invalidateResetToken(request.getEmail());
            emailService.sendPasswordChangedNotification(request.getEmail());

            return ResponseEntity.ok(new ApiResponse(true, "Password has been reset successfully"));
        } catch (PasswordRecoveryException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, ex.getMessage()));
        }
    }

  
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                        Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Authentication required"));
        }

        String usernameOrEmail = authentication.getName(); 


        User user = userRepository.findByEmail(usernameOrEmail)
                .or(() -> userRepository.findByUsername(usernameOrEmail))
                .orElseThrow(() -> new PasswordRecoveryException("Account not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Current password is incorrect"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        emailService.sendPasswordChangedNotification(usernameOrEmail);

        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }
}
