package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 매니저 페이지 요약 응답 DTO
 * - 상단: 매니저 기본정보
 * - 하단: 매니저가 담당한 처리 리스트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerSummaryDTO {
    private ManagerInfoDTO manager;     // 매니저 기본정보
    private List<TaskItemDTO> tasks;    // 처리 리스트
}
