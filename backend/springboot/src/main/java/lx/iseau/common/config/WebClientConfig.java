package lx.iseau.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	/*
	 * 비동기식 HTTP 요청을 처리하기 위해 사용되는 클라이언트
	 */
	@Bean
	public WebClient.Builder webClient() {
		return WebClient.builder();
	}
}
