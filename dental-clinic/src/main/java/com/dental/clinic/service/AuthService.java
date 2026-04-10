package com.dental.clinic.service;

import com.dental.clinic.dto.request.*;
import com.dental.clinic.dto.response.*;
import com.dental.clinic.exception.*;
import com.dental.clinic.model.*;
import com.dental.clinic.repository.*;
import com.dental.clinic.service.impl.UserDetailsServiceImpl;
import com.dental.clinic.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PatientService patientService;

    @Value("${app.password-reset.token-expiry-minutes:15}")
    private int tokenExpiryMinutes;

    @Value("${app.password-reset.otp-expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${app.registration.otp-expiry-minutes:5}")
    private int registrationOtpExpiryMinutes;



    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", null));

        if (!user.isActive()) {
            throw new BadRequestException("Account is not verified yet. Please check your email for the OTP.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }


    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            User existingUser = userRepository.findByEmail(request.getEmail()).orElseThrow();
            if (!existingUser.isEmailVerified()) {

                sendRegistrationOtp(existingUser.getEmail());
                return mapToUserResponse(existingUser);
            }
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.Role.PATIENT)
                .phoneNumber(request.getPhoneNumber())
                .clinicId(request.getClinicId())
                .emailVerified(false)
                .active(false)
                .build();

        user = userRepository.save(user);

        if (user.getRole() == User.Role.PATIENT) {
            patientService.createPatientFromUser(user);
        }

        log.info("New user registered (pending OTP): {} with role {}", user.getEmail(), user.getRole());
        sendRegistrationOtp(user.getEmail());

        return mapToUserResponse(user);
    }


    @Transactional
    public void verifyRegistrationOtp(String email, String otp) {
        OtpVerification otpRecord = otpVerificationRepository
                .findTopByEmailAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(
                        email, OtpVerification.OtpType.REGISTRATION)
                .orElseThrow(() -> new BadRequestException("No pending OTP found for this email. Please register again."));

        if (otpRecord.isExpired()) {
            throw new BadRequestException("OTP has expired. Please register again to receive a new OTP.");
        }
        if (!otpRecord.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid OTP. Please check and try again.");
        }

        otpRecord.setUsed(true);
        otpVerificationRepository.save(otpRecord);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEmailVerified(true);
        user.setActive(true);
        userRepository.save(user);

        log.info("OTP verified and account activated for: {}", email);
    }


    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));
        user.setEmailVerified(true);
        user.setActive(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());
    }


    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required for password reset");
        }

        userRepository.findByEmail(email).ifPresent(user -> {
            String method = request.getMethod() != null ? request.getMethod().toUpperCase() : "OTP_EMAIL";

            if ("EMAIL".equals(method)) {
                String token = UUID.randomUUID().toString();
                saveResetToken(email, token, null, tokenExpiryMinutes, PasswordResetToken.TokenType.EMAIL_LINK);
                emailService.sendPasswordResetEmail(email, token);
            } else {

                String otp = generateOtp();
                saveResetToken(email, UUID.randomUUID().toString(), otp,
                        otpExpiryMinutes, PasswordResetToken.TokenType.OTP_EMAIL);
                emailService.sendPasswordResetOtpEmail(email, otp);
            }
            log.info("Password reset OTP sent to: {}", email);
        });
    }

    @Transactional
    public void resetPassword(ConfirmPasswordResetRequest request) {
        PasswordResetToken resetToken;

        if (request.getOtp() != null && request.getEmail() != null) {
            resetToken = tokenRepository
                    .findByEmailAndOtpAndUsedFalse(request.getEmail(), request.getOtp())
                    .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));
        } else if (request.getToken() != null) {
            resetToken = tokenRepository.findByToken(request.getToken())
                    .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        } else {
            throw new BadRequestException("Either token or OTP + email is required");
        }

        if (resetToken.isUsed())    throw new BadRequestException("This reset token has already been used");
        if (resetToken.isExpired()) throw new BadRequestException("Reset token has expired. Please request a new one.");

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for: {}", user.getEmail());
    }


    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
            throw new BadRequestException("Invalid refresh token");
        }
        String newAccessToken = jwtUtil.generateToken(userDetails);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", null));
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }


    private void sendRegistrationOtp(String email) {
        otpVerificationRepository.deleteAllByEmailAndOtpType(email, OtpVerification.OtpType.REGISTRATION);
        String otp = generateOtp();
        OtpVerification record = OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiry(LocalDateTime.now().plusMinutes(registrationOtpExpiryMinutes))
                .otpType(OtpVerification.OtpType.REGISTRATION)
                .used(false)
                .build();
        otpVerificationRepository.save(record);
        new Thread(() -> emailService.sendRegistrationOtpEmail(email, otp)).start();
        log.info("Registration OTP sent to: {}", email);
    }

    private void saveResetToken(String email, String token, String otp,
                                int expiryMinutes, PasswordResetToken.TokenType type) {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email).token(token).otp(otp)
                .expiry(LocalDateTime.now().plusMinutes(expiryMinutes))
                .tokenType(type).used(false).build();
        tokenRepository.save(resetToken);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .clinicId(user.getClinicId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}