package lx.iseau.feature.fcm;

import lx.iseau.feature.fcm.TokenRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FcmDAO {
    // 토큰이 이미 존재하면 업데이트하고, 없으면 삽입 (UPSERT)
    void upsertToken(TokenRequest tokenRequest);

    // 알림 발송을 위해 특정 사용자의 토큰을 조회
    String getTokenByUserId(@Param("userId") String userId);
}
