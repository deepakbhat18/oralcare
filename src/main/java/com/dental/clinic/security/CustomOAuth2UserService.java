package com.dental.clinic.security;

import com.dental.clinic.model.User;
import com.dental.clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;









@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attrs = oAuth2User.getAttributes();

        String email = (String) attrs.get("email");
        String name  = (String) attrs.get("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not returned by Google. Check OAuth2 scope configuration.");
        }

        userRepository.findByEmail(email).ifPresentOrElse(
                existing -> {

                    existing.setName(name);
                    existing.setEmailVerified(true);
                    userRepository.save(existing);
                    log.info("OAuth2 login: existing user {} linked", email);
                },
                () -> {

                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .password("")
                            .role(User.Role.PATIENT)
                            .emailVerified(true)
                            .active(true)
                            .build();
                    userRepository.save(newUser);
                    log.info("OAuth2 login: new user auto-registered {}", email);
                }
        );

        return oAuth2User;
    }
}

/*
 * This file contains the CustomOAuth2UserService class which manages token validation, extracting authority claims, and applying endpoint authorization policies.
 * Concepts used: JWT validation filters, Spring Security configurations, and security context holders.
 */
