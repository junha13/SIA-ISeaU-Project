package lx.iseayou.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .cors(cors -> {})   // 이 줄이 있어야 WebMvc의 CORS 매핑이 실제로 적용됨
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()   // 모든 요청 허용
        )
        .csrf(csrf -> csrf.disable())   // CSRF 끔
        .formLogin(form -> form.disable()) // 로그인 폼 비활성화
        .httpBasic(basic -> basic.disable()); // Basic Auth도 비활성화
    return http.build();
    }
  
}