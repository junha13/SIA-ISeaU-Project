package lx.iseau.feature.controltower;

/**
 * 관제센터에 소속된 매니저(=tb_user의 user_number를 PK로 사용하는 tb_manager.manager_number) 요약 정보
 */
public class ManagerSummaryDTO {
    private Integer managerNumber; // = tb_user.user_number (설계상 동일키)
    private String userName;
    private String mobile;
    private String email;

    public Integer getManagerNumber() { return managerNumber; }
    public void setManagerNumber(Integer managerNumber) { this.managerNumber = managerNumber; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}