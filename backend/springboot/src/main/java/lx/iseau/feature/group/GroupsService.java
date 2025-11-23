package lx.iseau.feature.group;

import lx.iseau.feature.fcm.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;

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
    private final NotificationService notificationService;
    private static final String DEFAULT_LEADER_COLOR = "#0B1956";

    // --- 1. 그룹 생성 ---
    @Transactional
    public Map<String, Object> createGroup(RequestGroupDTO dto) {
        Map<String, Object> map = new HashMap<>();

        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");

        dto.setGroupName(dto.getGroupName().trim());
        dto.setGroupLeader(userNumber);

        if (dao.findGroupIdByUser(userNumber) != null) {
            return createErrorResponse("이미 그룹에 속해있습니다. 새로운 그룹을 만들 수 없습니다.");
        }

        try {
            dao.insertGroup(dto);

            Map<String, Object> memberParams = new HashMap<>();
            memberParams.put("groupNumber", dto.getGroupNumber());
            memberParams.put("userNumber", userNumber);
            memberParams.put("markerColor", DEFAULT_LEADER_COLOR);
            memberParams.put("status", "ACCEPTED");

            dao.insertGroupMember(memberParams);

            map.put("success", true);
            map.put("newGroupId", dto.getGroupNumber());

        } catch (DuplicateKeyException e) {
            return createErrorResponse("그룹 이름이 이미 존재합니다.");
        } catch (RuntimeException e) {
            return createErrorResponse("DB 처리 중 오류 발생: " + e.getMessage());
        }

        return map;
    }

    // --- 2. 그룹 목록 조회 ---
    @Transactional(readOnly = true)
    public Map<String, Object> getGroupsList() {
        Map<String, Object> map = new HashMap<>();

        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) {
            map.put("result", Collections.emptyList());
            map.put("message", "로그인이 필요합니다.");
            return map;
        }

        List<ResponseGroupListItemDTO> groupList = dao.findGroupsByUserNumber(userNumber);
        map.put("result", groupList);
        map.put("success", true);
        return map;
    }

 // --- 3. 그룹 멤버 초대 ---
    @Transactional
    public Map<String, Object> inviteGroupMember(RequestGroupInviteDTO request) {

        //상단 유효성 검사 로직
        Integer inviterNumber = getLoggedInUserNumber();
        if (inviterNumber == null) return createErrorResponse("로그인이 필요합니다.");
        
        Integer targetNumber = dao.findUserNumberById(request.getTargetUserId());
        if (targetNumber == null) return createErrorResponse("대상자를 찾을 수 없습니다.");
        
        if (dao.findGroupIdByUser(targetNumber) != null) {
            return createErrorResponse("대상자는 이미 그룹에 있거나 초대 대기 중입니다.");
        }

        request.setTargetUserNumber(targetNumber);

        Map<String, Object> memberParams = new HashMap<>();
        memberParams.put("groupNumber", request.getGroupId());
        memberParams.put("userNumber", targetNumber);
        memberParams.put("markerColor", request.getMarkerColor());
        memberParams.put("status", "PENDING");

        try {
            // 1. DB 저장
            int insertedRows = dao.insertGroupMember(memberParams);
            if (insertedRows == 0) return createErrorResponse("초대 저장 실패.");

           
            Integer invitationId = 0; // 변수명 변경
            
            // 대기 중인 목록에서 방금 넣은 ID 찾기
            List<Map<String, Object>> pendingList = dao.findPendingInvitationsByMember(targetNumber);
            
            for (Map<String, Object> invite : pendingList) {
                Object gIdObj = invite.get("groupNumber");
                if (gIdObj == null) gIdObj = invite.get("GROUPNUMBER");
                
                if (gIdObj != null && Integer.parseInt(gIdObj.toString()) == request.getGroupId()) {
                    Object idObj = invite.get("id");
                    if (idObj == null) idObj = invite.get("ID");
                    
                    if (idObj != null) {
                        invitationId = Integer.parseInt(idObj.toString()); // 여기에 할당
                        break; 
                    }
                }
            }
            
            System.out.println("🔎 [FCM] 조회된 초대장 ID: " + invitationId);

            // =================================================================
            // 🚨 [FCM 발송]
            // =================================================================
            try {
                String targetId = String.valueOf(targetNumber);
                String inviterName = request.getInviterName();
                if (inviterName == null || inviterName.isEmpty()) inviterName = "그룹장";

                notificationService.sendInviteNotification(
                        targetId,
                        inviterName,
                        request.getGroupId(),
                        invitationId // 0이 아니라 진짜 ID가 들어감
                );
                System.out.println("🚀 [FCM] 발송 성공 to " + targetId);
            } catch (Exception e) {
                System.err.println("❌ [FCM] 발송 실패: " + e.getMessage());
            }

        } catch (DuplicateKeyException e) {
            return createErrorResponse("이미 초대된 사용자입니다.");
        }

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        return successResponse;
    }
    // --- 4. 그룹 초대 수락 ---
    @Transactional
    public Map<String, Object> acceptLocationShare(RequestLocationShare request) {
        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");

        if (request.getInvitationId() == null) return createErrorResponse("그룹 ID가 누락되었습니다.");

        if (dao.findGroupIdByUser(userNumber) != null) {
            Map<String, Object> rejectParams = new HashMap<>();
            rejectParams.put("invitationId", request.getInvitationId());
            rejectParams.put("userNumber", userNumber);
            rejectParams.put("status", "REJECTED");
            dao.updateInviteStatus(rejectParams);
            return createErrorResponse("이미 다른 그룹에 속해있어 초대를 수락할 수 없습니다.");
        }

        Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("invitationId", request.getInvitationId());
        updateParams.put("userNumber", userNumber);
        updateParams.put("status", "ACCEPTED");

        try {
            int updatedRows = dao.updateInviteStatus(updateParams);
            if (updatedRows == 0) return createErrorResponse("초대 수락에 실패했습니다.");
        } catch (RuntimeException e) {
            return createErrorResponse("DB 처리 중 오류 발생: " + e.getMessage());
        }

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        return successResponse;
    }

    // --- 5. 그룹 초대 거절 ---
    @Transactional
    public Map<String, Object> rejectLocationShare(RequestLocationShare request) {
        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");

        if (request.getInvitationId() == null) return createErrorResponse("그룹 ID가 누락되었습니다.");

        Map<String, Object> statusParams = new HashMap<>();
        statusParams.put("invitationId", request.getInvitationId());
        statusParams.put("userNumber", userNumber);
        statusParams.put("status", "REJECTED");

        try {
            int deletedRows = dao.updateInviteStatus(statusParams);
            if (deletedRows == 0) return createErrorResponse("초대 거절에 실패했습니다.");
        } catch (RuntimeException e) {
            return createErrorResponse("DB 처리 중 오류 발생: " + e.getMessage());
        }

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        return successResponse;
    }

    // --- 6. 그룹 멤버 위치 조회 ---
    @Transactional(readOnly = true)
    public Map<String, Object> getGroupMemberLocations(ResponseGroupMemberLocationDTO dto) {
        Map<String, Object> map = new HashMap<>();
        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");

        Integer groupId = dao.findGroupIdByUser(userNumber);
        if (groupId == null) {
            map.put("result", Collections.emptyList());
            map.put("success", true);
            return map;
        }

        dto.setGroupNumber(groupId);
        List<ResponseGroupMemberLocationDTO> locationList = dao.findGroupMemberLocations(dto);
        map.put("result", locationList);
        map.put("success", true);
        return map;
    }

    // --- 7. 그룹 삭제 또는 탈퇴 ---
    @Transactional
    public Map<String, Object> deleteOrLeaveGroup() {
        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");

        Integer groupId = dao.findGroupIdByUser(userNumber);
        if (groupId == null) return createErrorResponse("소속된 그룹이 없습니다.");

        ResponseGroupDTO groupInfo = dao.findGroupByGroupNumber(groupId);
        if (groupInfo == null) return createErrorResponse("그룹 정보를 찾을 수 없습니다.");

        int groupLeaderNumber = groupInfo.getGroupLeader();

        if (userNumber.equals(groupLeaderNumber)) {
            dao.deleteGroupMembersByGroupId(groupId);
            dao.deleteGroupSettingsByGroupId(groupId);
            
            Map<String, Object> deleteParams = new HashMap<>();
            deleteParams.put("groupId", groupId);
            deleteParams.put("leaderUserNumber", userNumber);
            dao.deleteGroup(deleteParams);
        } else {
            Map<String, Object> leaveParams = new HashMap<>();
            leaveParams.put("groupNumber", groupId);
            leaveParams.put("userNumber", userNumber);
            dao.leaveGroupAsMember(leaveParams);
        }

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        return successResponse;
    }

    // --- 8. 내가 받은 초대 목록 조회 ---
    @Transactional(readOnly = true)
    public Map<String, Object> getPendingInvitations() {
        Map<String, Object> map = new HashMap<>();
        Integer userNumber = getLoggedInUserNumber();
        System.out.println("### [DEBUG] getPendingInvitations - 현재 세션 UserNumber: " + userNumber);
        if (userNumber == null) {
            map.put("success", false);
            map.put("message", "로그인이 필요합니다.");
            return map;
        }

        List<Map<String, Object>> invitationList = dao.findPendingInvitationsByMember(userNumber);
        map.put("result", invitationList);
        map.put("success", true);
        return map;
    }

    // --- 9. 알림 설정 저장 ---
    @Transactional
    public Map<String, Object> saveGroupSettings(RequestGroupSettingsDTO dto) {
        Map<String, Object> map = new HashMap<>();
        Integer userNumber = (Integer) session.getAttribute("userNumber");

        if (userNumber == null) return createErrorResponse("로그인 세션이 만료되었습니다.");
        dto.setUserNumber(userNumber);

        if (dto.getGroupNumber() == null) {
            Integer foundGroupId = dao.findGroupIdByUser(userNumber);
            if (foundGroupId == null) return createErrorResponse("현재 속한 그룹이 없어 설정을 저장할 수 없습니다.");
            dto.setGroupNumber(foundGroupId);
        }

        Integer leaderNumber = dao.findGroupLeaderByGroupId(dto.getGroupNumber());
        if (leaderNumber == null || !leaderNumber.equals(userNumber)) {
            return createErrorResponse("그룹 설정 권한이 없습니다. (그룹장만 설정 변경 가능)");
        }

        try {
            dao.insertOrUpdateGroupSettings(dto);
            map.put("success", true);
            map.put("message", "설정이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            System.err.println("알림 설정 저장 오류: " + e.getMessage());
            return createErrorResponse("설정 저장 중 DB 처리 오류가 발생했습니다.");
        }
        return map;
    }

    // --- 10. 알림 설정 조회 ---
    @Transactional(readOnly = true)
    public Map<String, Object> getGroupSettings(int groupNumber) {
        Map<String, Object> map = new HashMap<>();
        if (groupNumber == 0) return createErrorResponse("그룹 ID가 누락되었습니다.");

        RequestGroupSettingsDTO settings = dao.getGroupSettings(groupNumber);

        if (settings == null) {
            settings = new RequestGroupSettingsDTO();
            settings.setGroupNumber(groupNumber);
            settings.setUserNumber(0);
            settings.setTideAlert("N");
            settings.setGroupLeaveLevel1Alert("N");
            settings.setGroupLeaveLevel2Alert("N");
            settings.setGroupLeaveLevel3Alert("N");
            settings.setGroupLeaveLevel1Distance(0.0);
            settings.setGroupLeaveLevel2Distance(0.0);
            settings.setGroupLeaveLevel3Distance(0.0);
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("settings", settings);
        map.put("success", true);
        map.put("data", dataMap);
        return map;
    }

    // --- Helper Methods ---
    private Integer getLoggedInUserNumber() {
        Object userNumberObj = session.getAttribute("userNumber");
        if (userNumberObj == null) return null;
        try {
            return (Integer) userNumberObj;
        } catch (ClassCastException e) {
            session.invalidate();
            return null;
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", message);
        responseMap.put("success", false);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("result", "false");
        dataMap.put("message", message);
        responseMap.put("data", dataMap);
        return responseMap;
    }
}