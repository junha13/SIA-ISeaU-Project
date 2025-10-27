package lx.iseau.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    // application.properties에서 설정한 경로를 주입받습니다.
    // 경로: classpath:firebase/service-account.json
    @Value("${fcm.firebase-config-path}")
    private String firebaseConfigPath;

    private final ResourceLoader resourceLoader;

    /**
     * Firebase Admin SDK를 초기화하고 FirebaseMessaging 인스턴스를 Spring Bean으로 등록합니다.
     * 이 Bean은 NotificationService에서 알림 발송을 위해 주입받아 사용됩니다.
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        // Firebase 앱이 이미 초기화되었는지 확인합니다.
        // Spring Boot의 빠른 재시작 시 중복 초기화를 방지합니다.
        if (FirebaseApp.getApps().isEmpty()) {

            // 1. service-account.json 리소스 파일을 로드합니다.
            Resource resource = resourceLoader.getResource(firebaseConfigPath);

            if (!resource.exists()) {
                throw new IOException("Firebase service-account.json file not found at: " + firebaseConfigPath);
            }

            try (InputStream serviceAccount = resource.getInputStream()) {
                // 2. Google 자격 증명을 사용하여 Firebase 옵션을 설정합니다.
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        // Firebase 프로젝트 ID를 명시적으로 설정할 필요는 없지만,
                        // JSON 파일에서 자동으로 읽어옵니다. (isetDatabaseUrl 등 추가 가능)
                        .build();

                // 3. FirebaseApp 초기화
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK successfully initialized.");
            }
        }

        // 4. FirebaseMessaging 인스턴스를 반환하여 Spring Bean으로 등록합니다.
        return FirebaseMessaging.getInstance();
    }
}