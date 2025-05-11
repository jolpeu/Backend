package me.jimin.springbootdeveloper.service.auth;


import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.domain.User;
import me.jimin.springbootdeveloper.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(request);
        String provider = request.getClientRegistration().getRegistrationId(); // "google" 또는 "naver"

        // 공통 속성 추출 (provider 별 분기)
        String email;
        String name;

        if ("google".equals(provider)) {
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
        } else if ("naver".equals(provider)) {
            Map<String, Object> response = oauth2User.getAttribute("response");
            email = (String) response.get("email");
            name = (String) response.get("name");
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + provider);
        }

        // DB에 사용자 저장
        userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .provider(provider)
                        .role("USER")
                        .build())
        );

        return oauth2User;
    }
}

