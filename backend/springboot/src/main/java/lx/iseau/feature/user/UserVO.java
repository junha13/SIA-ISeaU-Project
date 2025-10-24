package lx.iseau.feature.user;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
	
	private int userNumber; // user_number
	private String id; // username, 아이디
    private String password; // password
    private String userName; // username, 회원이름
    private String mobile; // mobile
    private String email; // email
    private LocalDate birthDate; //birth_date
    private String location; // logation
    private int roleNumber; // role_number
    
}
