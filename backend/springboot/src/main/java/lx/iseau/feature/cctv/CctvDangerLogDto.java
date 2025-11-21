package lx.iseau.feature.cctv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 매니저의 처리 리스트 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CctvDangerLogDto {
    
    private Integer dangerLogNumber;
    private Integer beachNumber;
    
    private Integer camNumber;
    private String streamId;
    private String camLabel;
    private String viewType;
    
    private Integer dangerCount;
    private LocalDateTime cratedAt;
}
