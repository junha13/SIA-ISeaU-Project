// WearAlertRequest.java (DTO)
package lx.iseau.feature.fcm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WearAlertRequest {
    // 경고를 보낸 사용자 ID (WearOS를 착용한 사용자)
    private String senderUserId;

    // 알림을 받아야 할 대상자 ID (보호자, 그룹 리더 등)
    private String targetUserId;

    private String alertMessage;
    private long timestamp;
}