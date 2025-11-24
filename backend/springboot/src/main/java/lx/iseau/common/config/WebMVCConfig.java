package lx.iseau.common.config;

import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    // ------------------------------------
    // 1. SPA 포워딩: Vue Router 경로 처리
    // ------------------------------------
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 1. 루트 경로 (메인 페이지)
        registry.addViewController("/").setViewName("forward:/index.html");

        // 2. 1단계 경로 (예: /login, /register, /group 등) - 점(.)이 없는 경우만
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");

        // 3. 2단계 이상 깊은 경로 (예: /control/cctv, /beach/1, /sos/first-aid/cases 등)
        registry.addViewController("/**/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/");
        // 업로드된 파일 서빙: http://localhost:8080/files/** -> 프로젝트 실행 디렉토리의 uploads/** (해파리 제보 쪽임)
        registry.addResourceHandler("/files/**").addResourceLocations("file:" + Paths.get("uploads").toAbsolutePath().toString() + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // [수정] 5173을 프론트엔드 포트인 5174로 변경합니다.
                .allowedOrigins("http://localhost:5173", "https://hellokiyo.ngrok.io","https://subterete-reedy-dillon.ngrok-free.dev", "https://iseau.kr")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
