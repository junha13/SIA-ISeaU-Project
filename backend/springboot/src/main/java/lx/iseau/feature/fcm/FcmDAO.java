package lx.iseau.feature.fcm;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FcmDAO {

    // XML에서 #{userNumber}, #{token}을 쓰고 있으므로 여기도 이름이 같아야 합니다.
    void upsertToken(@Param("userNumber") String userNumber, @Param("token") String token);

    // XML에서 #{userId}를 쓰고 있으므로 여기도 이름이 같아야 합니다.
    String getTokenByUserNumber(@Param("userId") String userId);
}