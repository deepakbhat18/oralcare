package com.dental.clinic.security;

import com.dental.clinic.model.User;
import com.dental.clinic.repository.UserRepository;
import com.dental.clinic.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.dental.clinic.util.JwtUtil;
import java.io.IOException;










@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    private final JwtUtil             jwtUtil;
    private final UserDetailsService  userDetailsService;
    private final UserRepository      userRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 success: " + email));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("clinicId", user.getClinicId());
        String accessToken  = jwtUtil.generateToken(extraClaims, userDetails);
        String ipAddress = request.getRemoteAddr();
        String deviceDetails = request.getHeader("User-Agent");
        String refreshToken = refreshTokenService.createRefreshToken(user.getId(), ipAddress, deviceDetails).getToken();

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        log.info("OAuth2 success for {} — redirecting to frontend", email);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

/*
 * This file contains the OAuth2SuccessHandler class which manages token validation, extracting authority claims, and applying endpoint authorization policies.
 * Concepts used: JWT validation filters, Spring Security configurations, and security context holders.
 */
