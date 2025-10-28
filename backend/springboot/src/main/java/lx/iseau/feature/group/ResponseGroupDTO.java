package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class ResponseGroupDTO {


    private Integer groupNumber;  
    private Integer groupLeader;
    private Integer groupMember;
    private String groupName;   
    private String groupAccepted;
    private String markerColor;
}