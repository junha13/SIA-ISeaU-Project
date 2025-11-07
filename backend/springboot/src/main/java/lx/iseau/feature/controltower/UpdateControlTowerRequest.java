package lx.iseau.feature.controltower;

/** 관제센터 수정(PATCH) 요청 - 이름만 변경(최소기능) */
public class UpdateControlTowerRequest {
    private String controlTowerName;

    public String getControlTowerName() { return controlTowerName; }
    public void setControlTowerName(String controlTowerName) { this.controlTowerName = controlTowerName; }
}