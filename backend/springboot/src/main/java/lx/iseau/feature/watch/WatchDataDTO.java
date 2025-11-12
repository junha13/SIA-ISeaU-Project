// lx.iseau.feature.watch.WatchDataDTO.java
package lx.iseau.feature.watch;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchDataDTO {

    private Long watchNumber; // INSERT 후 MyBatis에 의해 채워짐 (keyProperty)
    private Integer heartRate;
    private Instant occurredAt; // PostgreSQL timestamp에 매핑
    private Integer userNumber;
}