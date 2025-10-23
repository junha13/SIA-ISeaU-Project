package lx.iseau.feature.auth;

public class ResetPasswordDTO {
    private String newPassword;
    private int userNumber;
    
    // Getter and Setters
    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    public int getUserNumber() {
        return userNumber;
    }
    public void setUserNumber(int userNumber) {
        this.userNumber = userNumber;
    }
}
