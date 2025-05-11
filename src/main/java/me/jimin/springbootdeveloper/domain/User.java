package me.jimin.springbootdeveloper.domain;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private String id;
    private String email;
    private String password; // 자체 로그인용
    private String name;
    private String provider; // "local", "google", "naver"
    private String role;     // "ROLE_USER", "ROLE_ADMIN"
}



