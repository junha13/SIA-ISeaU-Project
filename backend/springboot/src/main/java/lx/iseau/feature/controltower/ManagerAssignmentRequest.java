package lx.iseau.feature.controltower;

/**
 * 관제센터에 매니저 배정 요청.
 * 주의: tb_manager.manager_number는 SERIAL이지만 FK로 tb_user.user_number를 참조합니다.
 *      => 실제로는 "기존 사용자(user_number)"를 그대로 manager_number로 INSERT 해야 FK가 성립합니다.
 *      => 따라서 여기서는 managerUserNumber를 그대로 manager_number로 사용합니다.
 */
public class ManagerAssignmentRequest {
    private Integer managerUserNumber; // tb_user.user_number

    public Integer getManagerUserNumber() { return managerUserNumber; }
    public void setManagerUserNumber(Integer managerUserNumber) { this.managerUserNumber = managerUserNumber; }
}