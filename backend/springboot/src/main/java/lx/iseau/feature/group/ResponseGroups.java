package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

// --- 공통 응답 ---
@Data
@NoArgsConstructor
@AllArgsConstructor
class SuccessResponse {
    private boolean success = true; // 기본값 true
}