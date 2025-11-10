package lx.iseau.feature.group;

import lx.iseau.feature.group.RequestGroupDTO;
import lx.iseau.feature.group.RequestGroupInviteDTO;
import lx.iseau.feature.group.ResponseGroupListItemDTO;
import lx.iseau.feature.group.ResponseGroupMemberLocationDTO;
// import lx.iseau.feature.group.RequestLocationShare; // Service에서 사용되므로 DAO에서 불필요
import lx.iseau.feature.group.ResponseGroupDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // 복합 파라미터 사용 시 필요

import java.util.List;
import java.util.Map;

@Mapper
public interface GroupsDAO {

	// --- 그룹 생성/조회 (tb_group) ---
    
    // 1. 그룹 정의 생성 (DTO에 groupNumber가 keyProperty로 채워져야 함)
	int insertGroup(RequestGroupDTO dto); 
    
    // 2. 그룹 이름 중복 체크 (리더 기준)
	int SelectdoubleCheckByGroupName(RequestGroupDTO dto);
    
    // 3. 특정 그룹 ID로 그룹 정보(리더, 이름) 조회
    ResponseGroupDTO findGroupByGroupNumber(@Param("groupNumber") int groupNumber);
	
	// --- 그룹 멤버/초대 관리 (tb_group_member) ---
    
    // 4. 그룹 멤버 추가/초대 (tb_group_member에 'ACCEPTED' 또는 'PENDING'으로 INSERT)
    // Map에는 groupNumber, userNumber, markerColor, status가 포함됨
    int insertGroupMember(Map<String, Object> params);

    // 5. 유저가 속한 그룹 ID 조회 (1유저 1그룹 정책 확인용)
    Integer findGroupIdByUser(@Param("userNumber") int userNumber); 

    // 6. 초대 상태 변경 (tb_group_member의 status를 UPDATE 또는 DELETE)
    // statusParams에는 invitationId(그룹번호), userNumber, status가 포함됨
    int updateInviteStatus(Map<String, Object> statusParams);
    
    // 7. 현재 로그인 사용자의 그룹 목록 조회 (tb_group_member와 tb_group 조인)
	List<ResponseGroupListItemDTO> findGroupsByUserNumber(@Param("userNumber") int userNumber);

	// --- 위치 조회 (tb_group_member, tb_user) ---

    // 8. 그룹 멤버 위치 및 거리 조회
	List<ResponseGroupMemberLocationDTO> findGroupMemberLocations(ResponseGroupMemberLocationDTO dto);
	
	// --- 삭제/탈퇴 ---

    // 9. 그룹 설정 삭제 (tb_group_settings)
    int deleteGroupSettingsByGroupId(@Param("groupId") int groupId);

    // 10. 멤버 전체 삭제 (그룹 삭제 시 사용)
    int deleteGroupMembersByGroupId(@Param("groupId") int groupId);
    
    // 11. 그룹 정의 삭제 (그룹장 권한 확인)
    int deleteGroup(Map<String, Object> params); 

    // 12. 그룹 탈퇴 (그룹원)
    // params에는 groupNumber, userNumber가 포함됨
    int leaveGroupAsMember(Map<String, Object> params);

	// --- 헬퍼 메서드 ---
    
    // 13. ID(string)로 user_number(int) 찾기
	Integer findUserNumberById(@Param("userId") String userId); 
    
    // 14. 그룹장 번호 조회 (GroupService에서 사용)
	Integer findGroupLeaderByGroupId(@Param("groupId") int groupId);
	
	// GroupsDAO.java 인터페이스 내부에 추가
	RequestGroupSettingsDTO getGroupSettings(@Param("groupNumber") int groupNumber);
	int insertOrUpdateGroupSettings(RequestGroupSettingsDTO dto);
	// PENDING 상태인 초대 목록 조회 (ResponseGroupListItemDTO 사용)
	List<ResponseGroupListItemDTO> findPendingInvitationsByMember(@Param("userNumber") int userNumber);
}