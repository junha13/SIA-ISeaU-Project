package lx.iseau.feature.auth;

import lombok.Data;

@Data
public class RegisterDTO {
	
	private String userName;
	private String email;
	private String mobile;
	private String birthDate;
	private String id;
	private String password;
	private String checkPassword;

}
