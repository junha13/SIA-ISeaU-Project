package lx.iseau.feature.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
	
	private String userName;
	private String email;
	private String mobile;
	private String birthDate;
	private String id;
	private String password;
	private String checkPassword;

}
