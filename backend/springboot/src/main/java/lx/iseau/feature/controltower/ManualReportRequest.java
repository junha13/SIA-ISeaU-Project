package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualReportRequest {
    private Integer userNumber;     // 신고당한/신고한 사용자 ID (필수)
    private Double latitude;        // 신고 지점 위도
    private Double longitude;       // 신고 지점 경도
    private String reportType;
    
    private Integer heartRate;      // 🚨 [추가] BPM 필드를 DTO에 포함 (NULL 값 전송 대비)
}