package lx.iseau.feature.user;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
	
	private int userNumber;
    private String password;
    private String id; // username
    private String userName; 
    private String mobile;
    private String email;
    private LocalDate birthDate;
    private String location;
    private int roleNumber;
}
