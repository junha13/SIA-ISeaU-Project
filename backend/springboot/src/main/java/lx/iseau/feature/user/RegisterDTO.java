package lx.iseau.feature.user;

public class RegisterDTO {
	private String name; // user_name
	private String email; // email
	private String mobile; // mobile
	private String id; // username
	private String pW; // password
	private String pWCheck;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getpW() {
		return pW;
	}
	public void setpW(String pW) {
		this.pW = pW;
	}
	public String getpWCheck() {
		return pWCheck;
	}
	public void setpWCheck(String pWCheck) {
		this.pWCheck = pWCheck;
	}
	
	
	
}
