package lx.iseau.feature.fcm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TokenRequest {
    private String token; // FCM 등록 토큰
    private String userId; // 사용자 식별자
}