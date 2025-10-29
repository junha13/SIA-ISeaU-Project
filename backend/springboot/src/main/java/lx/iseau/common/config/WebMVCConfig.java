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
        registry.addViewController("/**/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
    }

    // ------------------------------------
    // 2. 정적 리소스 & 업로드 파일 경로
    // ------------------------------------
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /resources/** -> classpath:/resources/
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("classpath:/resources/");

        // 업로드 파일 서빙: /files/** -> 프로젝트 root의 uploads/
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + Paths.get("uploads").toAbsolutePath().toString() + "/");
    }

    // ------------------------------------
    // 3. CORS 설정
    // ------------------------------------
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:5173",   // 로컬 개발용 포트
                        "https://hellokiyo.ngrok.io" // ngrok 도메인
                )
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
