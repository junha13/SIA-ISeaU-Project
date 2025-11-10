package lx.iseau.feature.group;

import lx.iseau.feature.group.RequestGroupInviteDTO;
import lx.iseau.feature.group.ResponseGroupListItemDTO;
import lx.iseau.feature.group.ResponseGroupMemberLocationDTO;
import lx.iseau.feature.group.RequestLocationShare;
import lx.iseau.feature.group.ResponseGroupDTO;
// import lx.iseau.feature.group.vo.GroupVO;
// import lx.iseau.feature.post.PostDAO; // 사용되지 않아 주석 처리

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException; // PK/UNIQUE 제약조건 위반 처리

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

    private static final String DEFAULT_LEADER_COLOR = "#0B1956";
    
    // --- 1. 그룹 생성 및 그룹장 멤버 추가 (tb_group + tb_group_member) ---
	@Transactional
	public Map<String, Object> createGroup(RequestGroupDTO dto) {
	    Map<String, Object> map = new HashMap<>();
	    
	    // 1. 로그인 확인
	    Integer userNumber = getLoggedInUserNumber();
	    if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");
        
	    // 2. DTO 유효성 검사 및 데이터 준비
	    dto.setGroupName(dto.getGroupName().trim());
        dto.setGroupLeader(userNumber); 
	    
	    // 3. [수정] 이미 그룹에 속해있는지 확인 (1유저 1그룹 정책)
	    if (dao.findGroupIdByUser(userNumber) != null) {
	        return createErrorResponse("이미 그룹에 속해있습니다. 새로운 그룹을 만들 수 없습니다.");
	    }
	    
	    try {
	        // 4. tb_group에 그룹 정의 생성 (DTO에 groupNumber 자동 채워짐)
	        dao.insertGroup(dto); 
	        
	        // 5. [수정] tb_group_member에 그룹장 본인을 멤버로 추가 ('ACCEPTED' 상태)
	        Map<String, Object> memberParams = new HashMap<>();
	        memberParams.put("groupNumber", dto.getGroupNumber());
	        memberParams.put("userNumber", userNumber);
	        memberParams.put("markerColor", DEFAULT_LEADER_COLOR);
	        memberParams.put("status", "ACCEPTED");
	        
	        dao.insertGroupMember(memberParams); 
	        
	        // 6. 결과 처리
	        map.put("success", true);
	        map.put("newGroupId", dto.getGroupNumber()); 
	        
	    } catch (DuplicateKeyException e) {
	        // 이 예외는 tb_group의 group_leader/group_name Unique 제약 때문에 발생 가능
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

		// 1. 로그인 확인
		Integer userNumber = getLoggedInUserNumber();
		if (userNumber == null) {
			map.put("result", Collections.emptyList());
			map.put("message", "로그인이 필요합니다."); 
			return map;
		}

		// 2. [수정] 로그인된 사용자의 번호로 그룹 목록을 조회 (tb_group_member JOIN)
		List<ResponseGroupListItemDTO> groupList = dao.findGroupsByUserNumber(userNumber);
		map.put("result", groupList);
        map.put("success", true);
		return map;
	}

	// --- 3. 그룹 멤버 초대 (tb_group_member에 'PENDING' 상태로 INSERT) ---
	@Transactional
	public Map<String, Object> inviteGroupMember(RequestGroupInviteDTO request) { 
		
		// 1. 로그인 확인
		Integer inviterNumber = getLoggedInUserNumber();
		if (inviterNumber == null) return createErrorResponse("로그인이 필요합니다.");       
		
		// 2. 입력 값 유효성 검사
		Integer targetNumber = dao.findUserNumberById(request.getTargetUserId()); 
		if (targetNumber == null) return createErrorResponse("대상자 정보를 찾을 수 없습니다.");        
		if (inviterNumber.equals(targetNumber)) return createErrorResponse("자기 자신을 초대할 수 없습니다."); 
		if (request.getGroupId() == null) return createErrorResponse("그룹 ID가 누락되었습니다."); 
			   
        // 3. [수정] 대상자가 이미 다른 그룹에 속해있는지 확인 (1유저 1그룹 정책)
        if (dao.findGroupIdByUser(targetNumber) != null) {
            return createErrorResponse("대상자는 이미 다른 그룹에 속해있거나, 대기 중인 초대가 있습니다.");
        }
        
		// 4. 데이터 준비
		request.setTargetUserNumber(targetNumber);
        
        // 5. [수정] DB 작업 수행 (tb_group_member에 'PENDING' 상태로 삽입)
        Map<String, Object> memberParams = new HashMap<>();
        memberParams.put("groupNumber", request.getGroupId());
        memberParams.put("userNumber", targetNumber);
        memberParams.put("markerColor", request.getMarkerColor());
        memberParams.put("status", "PENDING"); 

        try {
            int insertedRows = dao.insertGroupMember(memberParams); 
            
            if (insertedRows == 0) {
                return createErrorResponse("초대 저장 실패."); 
            }
        } catch (DuplicateKeyException e) {
             // 1유저 1그룹 정책에 의해 이미 초대받은 유저일 경우 발생
            return createErrorResponse("대상자는 이미 그룹에 속해있거나, 대기 중인 초대가 있습니다.");
        }
        
		// TODO: FCM 알림 로직 추가
		
		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("success", true);
		return successResponse; 
	}
	
	// --- 4. 그룹 초대 수락 (tb_group_member의 status를 'ACCEPTED'로 UPDATE) ---
	@Transactional
	public Map<String, Object> acceptLocationShare(RequestLocationShare request) {              
		
		// 1. 로그인 확인
		Integer userNumber = getLoggedInUserNumber();
		if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");

		// 2. 입력값 유효성 검사
		if (request.getInvitationId() == null) {
			return createErrorResponse("그룹 ID가 누락되었습니다.");
		}
	
		// 3. [핵심] 1유저 1그룹 정책 검증 및 중복 초대 처리
		if (dao.findGroupIdByUser(userNumber) != null) {
		    // 이미 그룹에 속해 있다면, 이 초대는 거절 처리(DELETE)
		    Map<String, Object> rejectParams = new HashMap<>();
		    rejectParams.put("invitationId", request.getInvitationId());
		    rejectParams.put("userNumber", userNumber);
		    rejectParams.put("status", "REJECTED"); 
		    dao.updateInviteStatus(rejectParams);
		    
		    return createErrorResponse("이미 다른 그룹에 속해있어 초대를 수락할 수 없습니다.");
		}
	    
		// 4. DB 작업 수행 (ACCEPTED 상태로 업데이트)
		Map<String, Object> updateParams = new HashMap<>();
		updateParams.put("invitationId", request.getInvitationId()); 
		updateParams.put("userNumber", userNumber); 
		updateParams.put("status", "ACCEPTED");
	 
		try {
			int updatedRows = dao.updateInviteStatus(updateParams);
			
			if (updatedRows == 0) {
				return createErrorResponse("초대 수락에 실패했습니다. (유효한 초대가 아니거나 이미 처리되었습니다.)"); 
			}
		} catch (RuntimeException e) {
			return createErrorResponse("DB 처리 중 오류 발생: " + e.getMessage()); 
		}
		
		// 5. 결과 처리
		// TODO: FCM 알림 로직 추가 
		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("success", true);
		return successResponse; 
	}
	
	// --- 5. 그룹 초대 거절 (tb_group_member의 행 삭제) ---
	@Transactional
	public Map<String, Object> rejectLocationShare(RequestLocationShare request) {         
	    
	    // 1. 로그인 확인
	    Integer userNumber = getLoggedInUserNumber();
	    if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");
	    
	    // 2. 입력값 유효성 확인
	    if (request.getInvitationId() == null) {
	        return createErrorResponse("그룹 ID가 누락되었습니다.");
	    }    
	    
	    // 3. DB 작업 수행 (REJECTED 시 DELETE)
	    Map<String, Object> statusParams = new HashMap<>();
	    statusParams.put("invitationId", request.getInvitationId()); 	    	  
	    statusParams.put("userNumber", userNumber); 	    	
	    statusParams.put("status", "REJECTED"); // DAO에서 DELETE 쿼리 실행

	    try {
	        int deletedRows = dao.updateInviteStatus(statusParams);
	        
	        if (deletedRows == 0) {
	            return createErrorResponse("초대 거절에 실패했습니다. (유효한 초대가 아니거나 이미 처리되었습니다.)"); 
	        }
	        
	    } catch (RuntimeException e) {
	        return createErrorResponse("DB 처리 중 오류 발생: " + e.getMessage()); 
	    }
	    
	    // 4. 결과 처리
	    // TODO: FCM 알림 로직 추가

	    Map<String, Object> successResponse = new HashMap<>();
	    successResponse.put("success", true);
	    return successResponse; 
	}

	// --- 6. 그룹 멤버 위치 조회 ---
	@Transactional(readOnly = true)
	public Map<String, Object> getGroupMemberLocations(ResponseGroupMemberLocationDTO dto) {
		Map<String, Object> map = new HashMap<>();
        
        // 1. 로그인 확인
        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");

        // 2. 현재 사용자가 속한 그룹 ID 찾기
        Integer groupId = dao.findGroupIdByUser(userNumber);
        if (groupId == null) {
            map.put("result", Collections.emptyList());
            map.put("success", true);
            return map;
        }
        
        // 3. DTO에 조회 기준 그룹 정보 설정
        dto.setGroupNumber(groupId);

		List<ResponseGroupMemberLocationDTO> locationList = dao.findGroupMemberLocations(dto);
		map.put("result", locationList);
        map.put("success", true);
		return map;
	}
	
    // --- 7. 그룹 삭제 또는 탈퇴 ---
    @Transactional
    public Map<String, Object> deleteOrLeaveGroup() {
        
        // 1. 로그인 확인
        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) return createErrorResponse("로그인이 필요합니다.");

        // 2. 현재 사용자가 속한 그룹 ID 찾기
        Integer groupId = dao.findGroupIdByUser(userNumber);
        if (groupId == null) return createErrorResponse("소속된 그룹이 없습니다.");
        
        // 3. 그룹 정보 조회 및 리더 확인
        ResponseGroupDTO groupInfo = dao.findGroupByGroupNumber(groupId);
        if (groupInfo == null) return createErrorResponse("그룹 정보를 찾을 수 없습니다.");
        
        int groupLeaderNumber = groupInfo.getGroupLeader();
        
        // 4. [핵심] 리더와 본인 비교
        if (userNumber.equals(groupLeaderNumber)) {
            // 시나리오 1: 내가 그룹장이다 -> 그룹 전체 삭제 (트랜잭션)
            
            // 4-1. 멤버 삭제 (tb_group_member)
            dao.deleteGroupMembersByGroupId(groupId);
            
            // 4-2. 그룹 설정 삭제 (tb_group_settings)
            dao.deleteGroupSettingsByGroupId(groupId);
            
            // 4-3. 그룹 정의 삭제 (tb_group)
            Map<String, Object> deleteParams = new HashMap<>();
            deleteParams.put("groupId", groupId);
            deleteParams.put("leaderUserNumber", userNumber); 
            dao.deleteGroup(deleteParams);
            
        } else {
            // 시나리오 2: 내가 그룹원이다 -> 나만 탈퇴
            Map<String, Object> leaveParams = new HashMap<>();
            leaveParams.put("groupNumber", groupId);
            leaveParams.put("userNumber", userNumber);
            dao.leaveGroupAsMember(leaveParams);
        }

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        return successResponse;
    }
 // GroupsService.java 파일 내부에 추가

 // --- 8. 내가 받은 초대 목록 조회 ---
 // GroupsService.java 파일 내부에 추가

 // --- 8. 내가 받은 초대 목록 조회 ---
 @Transactional(readOnly = true)
 public Map<String, Object> getPendingInvitations() {
     Map<String, Object> map = new HashMap<>();

     // 1. 로그인 확인 (보안 최우선)
     Integer userNumber = getLoggedInUserNumber();
     System.out.println("### [DEBUG] getPendingInvitations - 현재 세션 UserNumber: " + userNumber);
     if (userNumber == null) {
         map.put("success", false);
         map.put("message", "로그인이 필요합니다."); 
         return map;
     }
     
     // 2. DAO를 호출하여 PENDING 상태인 초대 목록을 조회
     List<Map<String, Object>> invitationList = dao.findPendingInvitationsByMember(userNumber);

     map.put("result", invitationList);
     map.put("success", true);
     return map;
 }
    
    // --- 헬퍼 메서드 (Helper Methods) ---
 
 

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
        
        // 1. Vue의 'else' 블록이 response.data.message를 읽을 수 있도록 최상위에 추가
		responseMap.put("message", message);
        responseMap.put("success", false); // (혹시 모를 다른 로직을 위해 success:false도 유지)

        // 2. Vue의 'if' 블록이 'response.data.data.result'를 안전하게 읽을 수 있도록 data 구조 추가
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("result", "false"); // Vue가 'true'가 아님을 인지하도록 함
        dataMap.put("message", message); // 메시지 중복 포함 (안전 장치)
        responseMap.put("data", dataMap);
        
		return responseMap;
	}
	
	// --- 9. 알림 설정 저장/업데이트 ---
	@Transactional
	public Map<String, Object> saveGroupSettings(RequestGroupSettingsDTO dto) {
	    Map<String, Object> map = new HashMap<>();

	    // 1. 로그인 확인 및 userNumber를 DTO에 설정
	    Integer userNumber = (Integer) session.getAttribute("userNumber");

	    if (userNumber == null) {
	        return createErrorResponse("로그인 세션이 만료되었습니다.");
	    }
	    
	    // 💡 DTO에 userNumber 필드가 필요합니다. (RequestGroupSettingsDTO에 추가했다고 가정)
	    dto.setUserNumber(userNumber);

	    // 2. 그룹 ID 확인 및 설정 (DTO에 groupNumber가 없으면 현재 사용자의 그룹을 찾습니다.)
	    if (dto.getGroupNumber() == null) {
	        // 사용자가 현재 속한 그룹 ID를 찾습니다. (1인 1그룹 정책 기준)
	        Integer foundGroupId = dao.findGroupIdByUser(userNumber); 
	        
	        if (foundGroupId == null) {
	            return createErrorResponse("현재 속한 그룹이 없어 설정을 저장할 수 없습니다.");
	        }
	        dto.setGroupNumber(foundGroupId); 
	    }

	    // 3. 🚨 권한 검증: 그룹 설정은 리더만 변경 가능하다고 가정하고 리더 여부를 확인합니다.
	    Integer leaderNumber = dao.findGroupLeaderByGroupId(dto.getGroupNumber());
	    if (leaderNumber == null || !leaderNumber.equals(userNumber)) {
	        // 권한이 없으면 400 Bad Request와 유사한 오류를 반환합니다.
	        return createErrorResponse("그룹 설정 권한이 없습니다. (그룹장만 설정 변경 가능)");
	    }

	    // 4. DB 저장/업데이트
	    try {
	        dao.insertOrUpdateGroupSettings(dto);
	        map.put("success", true);
	        map.put("message", "설정이 성공적으로 저장되었습니다.");
	    } catch (Exception e) {
	        // DB 오류 등 처리
	        System.err.println("알림 설정 저장 오류: " + e.getMessage());
	        return createErrorResponse("설정 저장 중 DB 처리 오류가 발생했습니다.");
	    }
	    return map;
	}

	// --- 10. 알림 설정 조회 ---
	@Transactional(readOnly = true)
	public Map<String, Object> getGroupSettings(int groupNumber) {
	    Map<String, Object> map = new HashMap<>();

	    // 1. 그룹 ID 확인
	    if (groupNumber == 0) {
	        return createErrorResponse("그룹 ID가 누락되었습니다.");
	    }

	    // 2. DB에서 설정 조회
	    RequestGroupSettingsDTO settings = dao.getGroupSettings(groupNumber);

	    if (settings == null) {
	        // 설정이 없는 경우, 기본값 또는 빈 객체를 반환
	        settings = new RequestGroupSettingsDTO(); 
	        settings.setGroupNumber(groupNumber); 
	        settings.setUserNumber(0);
	        // CHAR(1) 필드 초기화
	        settings.setTideAlert("N"); 
	        settings.setGroupLeaveLevel1Alert("N");
	        settings.setGroupLeaveLevel2Alert("N");
	        settings.setGroupLeaveLevel3Alert("N");
	        
	        // Double 필드 초기화
	        settings.setGroupLeaveLevel1Distance(0.0);
	        settings.setGroupLeaveLevel2Distance(0.0);
	        settings.setGroupLeaveLevel3Distance(0.0);
	        
	        // DTO에 userNumber 필드가 있다면, 이 값도 0으로 초기화
	        // settings.setUserNumber(0);
	    }

	    Map<String, Object> dataMap = new HashMap<>();
	    dataMap.put("settings", settings); // settings를 data 안에 넣음

	    map.put("success", true);
	    map.put("data", dataMap); // 최상위 Map에 "data" 키로 삽입
	    return map;
	}
    // NOTE: validateAndGetPendingInvitation 헬퍼 메서드는 제거됨.

}