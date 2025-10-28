package lx.iseau.feature.group;

import lx.iseau.feature.group.GroupInviteRequestDTO;
import lx.iseau.feature.group.GroupListItemResponseDTO;
import lx.iseau.feature.group.GroupMemberLocationResponseDTO;
import lx.iseau.feature.group.InvitationDTO;
import lx.iseau.feature.group.LocationShareRequest;
import lx.iseau.feature.group.ResponseGroupDTO;
// import lx.iseau.feature.group.vo.GroupVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
// @RequiredArgsConstructor 제거
public class GroupsService {

    @Autowired
    private GroupsDAO DAO;

    // 임시 사용자 ID (테스트용) - 컨트롤러와 동일하게 사용
    private static final String TEMP_USER_ID = "tempUser";

    @Transactional(readOnly = true)
    public Map<String, Object> getGroupsList(String userId) { // 파라미터는 유지하되 내부에서 임시 ID 사용
        Map<String, Object> map = new HashMap<>();
        // 임시 ID로 사용자 번호 조회
        Integer userNumber = DAO.findUserNumberById(TEMP_USER_ID);
        if (userNumber == null) {
            map.put("result", Collections.emptyList());
            map.put("message", "사용자 정보(" + TEMP_USER_ID + ")를 찾을 수 없습니다."); // 로그 메시지 수정
            return map;
        }
        List<GroupListItemResponseDTO> groupList = DAO.findGroupsByUserNumber(userNumber);
        map.put("result", groupList);
        return map;
    }

    @Transactional
    public Map<String, Object> inviteGroupMember(GroupInviteRequestDTO request, String inviterUserId) { // 파라미터는 유지하되 내부에서 임시 ID 사용
        Map<String, Object> map = new HashMap<>();
        // 임시 ID를 초대자로 사용
        Integer inviterNumber = DAO.findUserNumberById(TEMP_USER_ID);
        Integer targetNumber = DAO.findUserNumberById(request.getTargetUserId());

        if (inviterNumber == null || targetNumber == null) {
            map.put("success", false);
            map.put("message", "초대자(" + TEMP_USER_ID + ") 또는 대상자 정보를 찾을 수 없습니다.");
            return map;
        }

        // REVIEW: DAO의 insertInvitation 파라미터 타입 확인 및 Map 사용 고려
        int insertedRows = DAO.insertInvitation(request);

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
        Integer userNumber = DAO.findUserNumberById(TEMP_USER_ID);
        if (userNumber == null) {
             map.put("success", false);
             map.put("message", "사용자 정보(" + TEMP_USER_ID + ")를 찾을 수 없습니다.");
             return map;
        }

        InvitationDTO invitation = DAO.findInvitationById(request.getInvitationId());
        // REVIEW: 유효성 검증 로직 추가 (대상자 비교 시 userNumber 사용)
        if (invitation == null /* || !invitation.getTargetUserNumber().equals(userNumber) 등 검증 실패 조건 */ ) {
             map.put("success", false);
             map.put("message", "유효하지 않거나 처리할 수 없는 초대입니다.");
             return map;
        }


        Map<String, Object> statusParams = new HashMap<>();
        statusParams.put("invitationId", request.getInvitationId());
        statusParams.put("status", "ACCEPTED");

        int updatedRows = DAO.updateInviteStatus(statusParams);
        if (updatedRows == 0) {
             map.put("success", false);
             map.put("message", "초대 상태 업데이트 실패");
             return map;
        }

        ResponseGroupDTO newMemberDto = new ResponseGroupDTO();
        // REVIEW: invitation 정보 + userNumber로 newMemberDto 필드 설정
        newMemberDto.setGroupMember(userNumber); // 예시: 수락자 번호 설정
        int insertedRows = DAO.insertGroupMember(newMemberDto);
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
        Integer userNumber = DAO.findUserNumberById(TEMP_USER_ID);
        if (userNumber == null) {
             map.put("success", false);
             map.put("message", "사용자 정보(" + TEMP_USER_ID + ")를 찾을 수 없습니다.");
             return map;
        }

        InvitationDTO invitation = DAO.findInvitationById(request.getInvitationId());
        // REVIEW: 유효성 검증 로직 추가 (대상자 비교 시 userNumber 사용)
        if (invitation == null /* || !invitation.getTargetUserNumber().equals(userNumber) 등 검증 실패 조건 */ ) {
             map.put("success", false);
             map.put("message", "유효하지 않거나 처리할 수 없는 초대입니다.");
             return map;
        }

        Map<String, Object> statusParams = new HashMap<>();
        statusParams.put("invitationId", request.getInvitationId());
        statusParams.put("status", "REJECTED");

        int updatedRows = DAO.updateInviteStatus(statusParams);
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
        List<GroupMemberLocationResponseDTO> locationList = DAO.findGroupMemberLocations(groupId);
        map.put("result", locationList);
        return map;
    }
}