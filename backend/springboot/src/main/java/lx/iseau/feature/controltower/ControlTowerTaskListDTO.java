package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControlTowerTaskListDTO {

    // t.task_number AS id
    private Integer id;

    // t.task_processed AS taskProcessed
    private Integer taskProcessed;

    // w.occurred_at AS dateAndTime
    private String dateAndTime;

    // u.birth_date AS birthDateForAge
    private String birthDateForAge;

    // u.gender AS gender
    private String gender;

    // user 위치 (ST_X/Y(u.location))
    private Double userLon;
    private Double userLat;

    // watch 위치 (ST_X/Y(w.watch_location))
    private Double watchLon;
    private Double watchLat;

    // w.heart_rate AS hr
    private Integer hr;

    // w.watch_count AS count
    private Integer count;

    // m.manager_number AS managerNumber
    private Integer managerNumber;

    // b.beach_name AS beachName
    private String beachName;
    private String type;
}