package lx.iseau.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

    /*
	@Bean
	public Interceptor loginInterceptor() {
		return new Interceptor();

	}
	
    @Override
	public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/**") // 모든 URL에 적용하되,
                .excludePathPatterns("/login", "/resources/**"); // 로그인 페이지와 리소스 파일은 제외
	}
	*/

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/resources/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // [수정] 5173을 프론트엔드 포트인 5174로 변경합니다.
                .allowedOrigins("http://localhost:5174", "https://hellokiyo.ngrok.io")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
