package lx.iseau.feature.watch;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 워치(스마트워치 이벤트) DTO
 *
 * - DB: tb_watch
 *   - watch_number  (SERIAL PK)
 *   - heart_rate    (INT, NOT NULL)
 *   - spo2          (INT, NOT NULL)
 *   - occurred_at   (TIMESTAMP, NOT NULL)
 *   - user_number   (INT, NOT NULL)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchEventDTO {
    /** PK (tb_watch.watch_number) */
    private Integer watchNumber;

    /** 심박수 (tb_watch.heart_rate) */
    private Integer heartRate;

    /** 산소포화도 (tb_watch.spo2) */
    private Integer spo2;

    /** 발생 시각 (tb_watch.occurred_at) */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurredAt;

    /** 이벤트 소유 사용자 번호 (tb_watch.user_number) */
    private Integer userNumber;
}
