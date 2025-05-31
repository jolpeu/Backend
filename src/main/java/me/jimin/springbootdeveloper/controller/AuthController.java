package me.jimin.springbootdeveloper.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.config.jwt.JwtTokenProvider;
import me.jimin.springbootdeveloper.domain.User;
import me.jimin.springbootdeveloper.dto.LoginRequest;
import me.jimin.springbootdeveloper.dto.LoginResponse;
import me.jimin.springbootdeveloper.dto.SignupRequest;
import me.jimin.springbootdeveloper.repository.UserRepository;
import me.jimin.springbootdeveloper.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 완료");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @GetMapping("/oauth/success")
    public void oauthLoginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User,
                                  HttpServletResponse response) throws IOException {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email;

        // ✅ 네이버 로그인인 경우: 응답이 중첩됨
        if (attributes.containsKey("response")) {
            Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
            email = (String) responseMap.get("email");
        } else {
            // ✅ 구글 로그인인 경우: 바로 email 존재
            email = (String) attributes.get("email");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());

        // Flutter 앱으로 리디렉션
        String redirectUrl = "http://localhost:3000/#/login-redirect?token=" + token;
        response.sendRedirect(redirectUrl);
    }


}