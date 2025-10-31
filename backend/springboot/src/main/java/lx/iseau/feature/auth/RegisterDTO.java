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
	private String mobile1;
	private String mobile2;
	private String mobile3;
	private String birthDate;
	private String id;
	private String password;
	private String checkPassword;

	/**
	 * getMobile
	 * - MyBatis 매퍼에서 #{registerDTO.mobile}로 접근할 때 호출됩니다.
	 * - mobile 필드가 비어있으면 mobile1-mobile2-mobile3 형식으로 합쳐 반환합니다.
	 */
	public String getMobile() {
		if (this.mobile != null && !this.mobile.isBlank()) {
			return this.mobile;
		}
		// mobile1/mobile2/mobile3가 모두 존재하면 합쳐서 반환
		if (this.mobile1 != null && this.mobile2 != null && this.mobile3 != null
				&& !this.mobile1.isBlank() && !this.mobile2.isBlank() && !this.mobile3.isBlank()) {
			return String.format("%s-%s-%s", this.mobile1.trim(), this.mobile2.trim(), this.mobile3.trim());
		}
		return this.mobile; // 기본값(null 또는 빈값)
	}

}
