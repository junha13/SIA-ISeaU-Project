package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관제센터 → 서버로 보내는 TTS 안내 방송 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TtsBroadcastRequest {

    // 어떤 해수욕장인지 (관제에서 선택한 beachNumber)
    private Integer beachNumber;

    // 오른쪽 상단에 뜨는 CCTV/장소 이름 (예: "애월 하귀 가문동 포구")
    private String cctvName;

    // 안내 방송 내용
    private String message;
}
