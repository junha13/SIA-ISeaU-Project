package lx.iseau.feature.fcm;

import lx.iseau.feature.fcm.TokenRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FcmDAO {

    // 1. userId(로그인 ID)로 DB의 user_number(PK)를 조회하는 메서드 추가
    Integer getUserNumberByUserId(@Param("userId") String userId);

    // 알림 발송을 위해 특정 사용자의 토큰을 조회
    String getTokenByUserNumber(@Param("userNumber") String userNumber);
    // 2. 토큰 저장/업데이트는 user_number를 사용하여 실행되도록 파라미터를 명시합니다.
    void upsertToken(@Param("userNumber") int userNumber, @Param("token") String token);

    // 3. 알림 발송을 위해 특정 사용자의 토큰을 user_number로 조회
    // DAO에서는 user_number를 사용하고, Service에서 userId -> userNumber 변환을 담당합니다.
    String getTokenByUserNumber(@Param("userNumber") int userNumber);
}
