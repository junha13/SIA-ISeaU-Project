package lx.iseau.feature.controltower;

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
public class TaskListDTO {

    // tb_task
    private Integer taskNumber;
    private Integer taskProcessed;  // 0/1

    // tb_watch
    private LocalDateTime occurredAt;
    
    // tb_user
    private String  birthDate;
    private String  gender;  
    
}
