package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 그룹 위치 조회 (GET /groups/locations) 시 사용되는 응답 DTO의 단일 항목 구조입니다.
 * API 명세: [ { id, lat, lng, name, color, status, lastUpdate } ]
 */
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class ResponseGroupMemberLocationDTO {
	
	private Integer leaderNumber;
	private Integer groupNumber;
    private Double myLatitude;
    private Double myLongitude;
    private String userStatus;
    
    //--
    private Integer beachNumber;
    private Integer id;
    private Double lat;
    private Double lng;
    private String name;
    private String color;
    private String status;
    private LocalDateTime lastUpdate;
    private Double distance; // 자신과 이 행(유저)과의 거리
    private String birthDate;
    
    // 🚨 [추가] 성별 표시를 위한 필드
    private String gender;
}