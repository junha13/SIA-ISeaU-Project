package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskLogDTO {

    // w.occurred_at AS dateAndTime
    private String forTime;

    // w.heart_rate AS hr
    private Integer hr;

}