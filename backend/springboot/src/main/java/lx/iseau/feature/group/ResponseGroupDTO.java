package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseGroupDTO {
	
    private Integer groupNumber;    // 그룹 PK
    private Integer groupLeader;    // 그룹장 user_number
    private String groupName;       // 그룹 이름
  
}