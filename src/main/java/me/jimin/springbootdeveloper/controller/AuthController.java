package me.jimin.springbootdeveloper.controller;

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
    public ResponseEntity<?> oauthLoginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        String email = (String) response.get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}