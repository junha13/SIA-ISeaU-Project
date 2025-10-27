package lx.iseau.feature.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO {
    private String newPassword;
    private int userNumber;

}
