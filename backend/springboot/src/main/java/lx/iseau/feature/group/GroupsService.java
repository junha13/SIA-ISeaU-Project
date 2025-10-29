package lx.iseau.feature.group;

import lx.iseau.feature.group.GroupInviteRequestDTO;
import lx.iseau.feature.group.GroupListItemResponseDTO;
import lx.iseau.feature.group.GroupMemberLocationResponseDTO;
import lx.iseau.feature.group.InvitationDTO;
import lx.iseau.feature.group.LocationShareRequest;
import lx.iseau.feature.group.ResponseGroupDTO;
// import lx.iseau.feature.group.vo.GroupVO;
import lx.iseau.feature.post.PostDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GroupsService {

    private final GroupsDAO dao;
    
	private final HttpSession session;

    // 그룹생성
    @Transactional
    public Map<String, Object> createGroup(RequestGroupDTO dto) { // 파라미터는 유지하되 내부에서 임시 ID 사용
    	Map<String, Object> map = new HashMap<>();
    	dto.setGroupName(dto.getGroupName().trim());
    	dto.setUserId((Integer) session.getAttribute("userNumber"));
    	int num = dao.insertGroup(dto);
    	
    	
    	map.put("result", num == 1 ? "true" : "false");
    	return map;
    }
    
    // 그룹명 더블체크
    @Transactional
    public Map<String, Object> doubleCheckGroupName(RequestGroupDTO dto) { // 파라미터는 유지하되 내부에서 임시 ID 사용
    	Map<String, Object> map = new HashMap<>();
    	
    	if(dto.getGroupName().trim().isEmpty()) return Map.of("result", "empty");
    	
    	dto.setGroupName(dto.getGroupName().trim());
    	dto.setUserId((Integer) session.getAttribute("userNumber"));
    	int num = dao.SelectdoubleCheckByGroupName(dto);
    	
    	
    	map.put("result", num == 0 ? "true" : "false"); // 중복 없어야 true
    	return map;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getGroupsList(String userId) { // 파라미터는 유지하되 내부에서 임시 ID 사용
        Map<String, Object> map = new HashMap<>();
        // 임시 ID로 사용자 번호 조회
        Integer userNumber = dao.findUserNumberById(userId);
        if (userNumber == null) {
            map.put("result", Collections.emptyList());
            map.put("message", "사용자 정보(" + userId + ")를 찾을 수 없습니다."); // 로그 메시지 수정
            return map;
        }
        List<GroupListItemResponseDTO> groupList = dao.findGroupsByUserNumber(userNumber);
        map.put("result", groupList);
        return map;
    }

    @Transactional
    public Map<String, Object> inviteGroupMember(GroupInviteRequestDTO request, String inviterUserId) { // 파라미터는 유지하되 내부에서 임시 ID 사용
        Map<String, Object> map = new HashMap<>();
        // 임시 ID를 초대자로 사용
        Integer inviterNumber = dao.findUserNumberById(inviterUserId);
        Integer targetNumber = dao.findUserNumberById(request.getTargetUserId());

        if (inviterNumber == null || targetNumber == null) {
            map.put("success", false);
            map.put("message", "초대자(" + inviterUserId + ") 또는 대상자 정보를 찾을 수 없습니다.");
            return map;
        }

        // REVIEW: DAO의 insertInvitation 파라미터 타입 확인 및 Map 사용 고려
        int insertedRows = dao.insertInvitation(request);

        if (insertedRows == 0) {
            map.put("success", false);
            map.put("message", "초대 저장 실패");
            return map;
        }
        // REVIEW: FCM 알림 로직 추가 필요

        map.put("success", true);
        return map;
    }

    @Transactional
    public Map<String, Object> acceptLocationShare(LocationShareRequest request, String userId) { // 파라미터는 유지하되 내부에서 임시 ID 사용
        Map<String, Object> map = new HashMap<>();

        // 임시 ID를 수락자로 사용
        Integer userNumber = dao.findUserNumberById(userId);
        if (userNumber == null) {
             map.put("success", false);
             map.put("message", "사용자 정보(" + userId + ")를 찾을 수 없습니다.");
             return map;
        }

        InvitationDTO invitation = dao.findInvitationById(request.getInvitationId());
        // REVIEW: 유효성 검증 로직 추가 (대상자 비교 시 userNumber 사용)
        if (invitation == null /* || !invitation.getTargetUserNumber().equals(userNumber) 등 검증 실패 조건 */ ) {
             map.put("success", false);
             map.put("message", "유효하지 않거나 처리할 수 없는 초대입니다.");
             return map;
        }


        Map<String, Object> statusParams = new HashMap<>();
        statusParams.put("invitationId", request.getInvitationId());
        statusParams.put("status", "ACCEPTED");

        int updatedRows = dao.updateInviteStatus(statusParams);
        if (updatedRows == 0) {
             map.put("success", false);
             map.put("message", "초대 상태 업데이트 실패");
             return map;
        }

        ResponseGroupDTO newMemberDto = new ResponseGroupDTO();
        // REVIEW: invitation 정보 + userNumber로 newMemberDto 필드 설정
        newMemberDto.setGroupMember(userNumber); // 예시: 수락자 번호 설정
        int insertedRows = dao.insertGroupMember(newMemberDto);
        if (insertedRows == 0) {
            map.put("success", false);
            map.put("message", "그룹 멤버 추가 실패");
            throw new RuntimeException("그룹 멤버 추가 실패 (DB 오류)");
        }
        // REVIEW: FCM 알림 로직 추가 (수락 알림)

        map.put("success", true);
        return map;
    }

    @Transactional
    public Map<String, Object> rejectLocationShare(LocationShareRequest request, String userId) { // 파라미터는 유지하되 내부에서 임시 ID 사용
        Map<String, Object> map = new HashMap<>();

        // 임시 ID를 거절자로 사용
        Integer userNumber = dao.findUserNumberById(userId);
        if (userNumber == null) {
             map.put("success", false);
             map.put("message", "사용자 정보(" + userId + ")를 찾을 수 없습니다.");
             return map;
        }

        InvitationDTO invitation = dao.findInvitationById(request.getInvitationId());
        // REVIEW: 유효성 검증 로직 추가 (대상자 비교 시 userNumber 사용)
        if (invitation == null /* || !invitation.getTargetUserNumber().equals(userNumber) 등 검증 실패 조건 */ ) {
             map.put("success", false);
             map.put("message", "유효하지 않거나 처리할 수 없는 초대입니다.");
             return map;
        }

        Map<String, Object> statusParams = new HashMap<>();
        statusParams.put("invitationId", request.getInvitationId());
        statusParams.put("status", "REJECTED");

        int updatedRows = dao.updateInviteStatus(statusParams);
        if (updatedRows == 0) {
            map.put("success", false);
            map.put("message", "초대 상태 업데이트 실패");
            return map;
        }
        // REVIEW: FCM 알림 로직 추가 (거절 알림)

        map.put("success", true);
        return map;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGroupMemberLocations(int groupId) {
        Map<String, Object> map = new HashMap<>();
        // REVIEW: 그룹 존재 여부, 조회 권한 확인 로직 추가 가능
        List<GroupMemberLocationResponseDTO> locationList = dao.findGroupMemberLocations(groupId);
        map.put("result", locationList);
        return map;
    }
}