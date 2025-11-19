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
        // 모든 요청을 index.html로 포워딩 (단, .js, .css 등 제외)
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
//        registry.addViewController("/**/{path:[^\\.]*}")
//                .setViewName("forward:/index.html");
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
                .allowedOrigins("http://localhost:5173", "https://subterete-reedy-dillon.ngrok-free.dev", "https://iseau.kr")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
