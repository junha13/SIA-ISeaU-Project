package lx.iseau;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "lx.iseau.feature")
public class SpringbootApplication {
	
   /*
   implements CommandLineRunner

   private final ApplicationContext context;

    public BackendApplication(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void run(String... args) {
        System.out.println("===== 스프링 부트 Bean 목록 =====");
        String[] beanNames = context.getBeanDefinitionNames();

        // Bean 이름 순서 정렬
        java.util.Arrays.sort(beanNames);

        for (String name : beanNames) {
            System.out.println(name + " : " + context.getBean(name).getClass().getName());
        }
	}*/

	public static void main(String[] args) {
		SpringApplication.run(SpringbootApplication.class, args);
		
        // ★★★ 확인을 위해 새로운 로그를 추가해 보세요! ★★★
        //logger.info("======= Logback 테스트 성공! =======");
        //logger.warn("======= 경고(WARN) 레벨 테스트 =======");
        //logger.error("======= 에러(ERROR) 레벨 테스트 =======");
	}

}
