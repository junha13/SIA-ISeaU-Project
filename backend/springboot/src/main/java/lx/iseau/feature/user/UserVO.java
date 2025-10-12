package lx.iseau.feature.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
	
	private int userNumber;
	private String id;
    private String password;
    private String userName; 
}
