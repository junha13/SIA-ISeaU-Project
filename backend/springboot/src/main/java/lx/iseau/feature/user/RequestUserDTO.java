package lx.iseau.feature.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestUserDTO {
	
	private Integer userNumber;
	private String id;         
	private String userName;   
    private String mobile;     
    private String email;      
    private Integer roleNumber; 
}