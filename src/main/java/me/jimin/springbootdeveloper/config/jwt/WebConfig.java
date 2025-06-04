package me.jimin.springbootdeveloper.config.jwt;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Flutter 앱에서 오는 모든 요청 허용
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);    // JWT 또는 쿠키 등 인증정보 허용
    }
}
