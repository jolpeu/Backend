package me.jimin.springbootdeveloper.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.config.jwt.JwtTokenProvider;
import me.jimin.springbootdeveloper.domain.User;
import me.jimin.springbootdeveloper.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // ✅ 구글/네이버 공통 이메일 추출
        String email;
        Map<String, Object> attributes = oAuth2User.getAttributes();
        if (attributes.containsKey("response")) {
            email = (String) ((Map<String, Object>) attributes.get("response")).get("email"); // 네이버
        } else {
            email = (String) attributes.get("email"); // 구글
        }

        // ✅ 사용자 정보 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // ✅ JWT 발급
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());

        response.sendRedirect("http://localhost:3000/#/login-redirect?token=" + token);
    }
}


