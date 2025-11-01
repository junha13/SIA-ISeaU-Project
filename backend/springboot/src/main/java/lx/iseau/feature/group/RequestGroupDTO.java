package lx.iseau.feature.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class RequestGroupDTO {

	private int userId;
	private String groupName;
	private Integer groupLeader;
	private Integer groupNumber;    
	private Integer groupMember;
	private String groupAccepted;
	private String markerColor;
}
