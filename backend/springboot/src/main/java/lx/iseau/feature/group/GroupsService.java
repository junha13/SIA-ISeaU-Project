package lx.iseau.feature.group;

import lx.iseau.feature.group.RequestGroupInviteDTO;
import lx.iseau.feature.group.ResponseGroupListItemDTO;
import lx.iseau.feature.group.ResponseGroupMemberLocationDTO;
import lx.iseau.feature.group.InvitationDTO;
import lx.iseau.feature.group.RequestLocationShare;
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

	// Group
	@Transactional
	public Map<String, Object> createGroup(RequestGroupDTO dto) {
	    Map<String, Object> map = new HashMap<>();
	    dto.setGroupName(dto.getGroupName().trim());
	    dto.setUserId((Integer) session.getAttribute("userNumber"));
	    
	    int num = dao.insertGroup(dto); // ⬅️ 이 DTO에 새 ID가 담깁니다.
	    
	    if (num == 1) {
	        map.put("result", "true");
	        // 💡 [추가] DTO에 저장된 새 그룹 ID를 응답에 포함
	        map.put("newGroupId", dto.getUserId()); 
	    } else {
	        map.put("result", "false");
	    }
	    
	    return map;
	}
	
	/**
	 * [수정됨]
	 * (userId 파라미터 제거)
	 * 현재 로그인한 사용자의 그룹 목록을 조회합니다.
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> getGroupsList() { 
		Map<String, Object> map = new HashMap<>();

		// 1. 세션에서 직접 '로그인한 사용자'의 번호를 가져옵니다.
		Integer userNumber = getLoggedInUserNumber();

		// 2. 로그인이 안 된 경우(null)를 안전하게 처리합니다.
		if (userNumber == null) {
			map.put("result", Collections.emptyList());
			map.put("message", "로그인이 필요합니다."); // 3. 메시지 명확화
			return map;
		}

		// 4. 로그인된 사용자의 번호로 그룹 목록을 조회합니다.
		List<ResponseGroupListItemDTO> groupList = dao.findGroupsByUserNumber(userNumber);
		map.put("result", groupList);
		return map;
	}

	/**
	 * 그룹 멤버를 초대합니다. (tb_group에 'N' 상태로 INSERT 가정)
	 */
	@Transactional
	public Map<String, Object> inviteGroupMember(RequestGroupInviteDTO request) { 
		
		// --- 1단계: 입력 값 유효성 검사 ---
		Integer inviterNumber = getLoggedInUserNumber(); // 현재 로그인 사용자 확인
		if (inviterNumber == null) return createErrorResponse("로그인이 필요합니다.");       
		Integer targetNumber = dao.findUserNumberById(request.getTargetUserId()); // 초대 대상 확인
		if (targetNumber == null) return createErrorResponse("대상자 정보를 찾을 수 없습니다.");        
		if (inviterNumber.equals(targetNumber)) return createErrorResponse("자기 자신을 초대할 수 없습니다."); // 자기 초대 방지     
		if (request.getGroupId() == null) return createErrorResponse("그룹 ID가 누락되었습니다."); // 그룹 ID 확인
			   // --- 2단계: 데이터 준비 ---
		request.setInviterUserNumber(inviterNumber); // DTO에 초대자 번호 설정
		request.setTargetUserNumber(targetNumber);
				// --- 3단계: DB 작업 수행 ---
		int insertedRows = dao.insertInvitation(request); // DB에 초대 정보 저장 시도
		// --- 4단계: 결과 처리 및 반환 ---
		if (insertedRows == 0) {
			// 저장 실패 시 (예: 이미 초대함) 에러 응답 반환
			return createErrorResponse("초대 저장 실패 (이미 초대한 경우일 수 있습니다.)"); 
		}
				// TODO: FCM 알림 로직 추가
		
		// 성공 응답 반환
		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("success", true);
		return successResponse; 
	}
	
	
	// GroupsService.java
	@Transactional(readOnly = true)
	public Map<String, Object> getPendingInvitations() {
	    
	    // 1. [필수] 현재 로그인한 사용자가 누구인지 확인합니다.
	    Integer userNumber = getLoggedInUserNumber();
	    
	    // 2. [필수] 로그인하지 않은 사용자는 조회할 수 없습니다.
	    if (userNumber == null) {
	        return createErrorResponse("로그인이 필요합니다.");
	    }
	    
	    // 3. [핵심] 로그인한 사용자의 초대 목록을 조회합니다.
	    List<InvitationDTO> invitations = dao.findPendingInvitationsByMember(userNumber);
	    
	    Map<String, Object> map = new HashMap<>();
	    map.put("invitations", invitations);
	    map.put("count", invitations.size());
	    return map;
	}
	/**
	 * 그룹 초대를 수락합니다. (tb_group의 'N' 상태를 'Y'로 UPDATE)
	 */
	@Transactional
	public Map<String, Object> acceptLocationShare(RequestLocationShare request) {              
		// 모든 유효성 검사(로그인, ID 유효성, 권한, 상태)를 헬퍼 메서드로 위임하고,
		InvitationDTO invitation = null;
		Integer userNumber = getLoggedInUserNumber(); // 현재 로그인 사용자(수락자)        
		try {
			// 헬퍼 메서드 호출
			invitation = validateAndGetPendingInvitation(userNumber, request.getInvitationId());
		
		} catch (RuntimeException e) {
			// 유효성 검사 실패 시 바로 에러 응답 반환
			return createErrorResponse(e.getMessage()); 
		}
		  
		//초대 상태 변경용 파라미터 (ACCEPTED)
		Map<String, Object> map = new HashMap<>();
		map.put("invitationId", request.getInvitationId());
		map.put("status", "ACCEPTED"); // mapper에서 'Y'로 변환하여 UPDATE
	 
		// [수정됨] 
		// '초대 수락'은 '새 그룹 생성(INSERT)'이 아니므로,
		// groupLeaderNumber 조회 및 RequestGroupDTO 생성 로직이 필요 없습니다.
		
		// --- 3단계: DB 작업 수행 (트랜잭션) 및 예외 처리 ---
		try {
			// 3-1. 초대 상태를 "ACCEPTED"로 변경 (tb_group의 'N' -> 'Y')
			int updatedRows = dao.updateInviteStatus(map);
			if (updatedRows == 0) {
				return createErrorResponse("초대 상태 업데이트에 실패했습니다."); 
			}
			
			// 3-2. ❌ [버그 수정] 그룹에 새 멤버로 추가하는 로직(insertGroup) 삭제
			// int insertedRows = dao.insertGroup(dto); // 이 코드가 버그의 원인입니다.

		} catch (RuntimeException e) {
			// DB 작업 중 발생한 RuntimeException(DB 오류) 처리
			return createErrorResponse(e.getMessage()); 
		}
		// --- 4단계: 결과 처리 및 반환 (변경 없음) ---       
		// TODO: FCM 알림 로직 추가 (초대자에게 수락 알림 보내기)
		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("success", true);
		return successResponse; 
	}
	
	/**
	 * 그룹 초대를 거절합니다. (tb_group의 'N' 상태 행 삭제)
	 */
	@Transactional
	public Map<String, Object> rejectLocationShare(RequestLocationShare request) {         
		//로그인 확인 및 유효성 검사를 헬퍼 메서드로 처리
		try {
			Integer userNumber = getLoggedInUserNumber();
			InvitationDTO invitation = validateAndGetPendingInvitation(userNumber, request.getInvitationId());
			
			// --- 2단계: DB 작업 수행 ---
			
			// 상태 변경용 Map (REJECTED)
			Map<String, Object> statusParams = new HashMap<>();
			statusParams.put("invitationId", invitation.getInvitationId()); // 헬퍼에서 받은 DTO 사용
			statusParams.put("status", "REJECTED"); // mapper에서 DELETE 실행

			int updatedRows = dao.updateInviteStatus(statusParams);
			
			if (updatedRows == 0) {
				return createErrorResponse("초대 상태 업데이트에 실패했습니다."); 
			}
			
			// 3. 결과 처리
			// TODO: FCM 알림 로직 추가

			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("success", true);
			return successResponse; 
		
		} catch (RuntimeException e) {
			// validateAndGetPendingInvitation에서 던진 예외를 여기서 잡습니다.
			return createErrorResponse(e.getMessage()); 
		}
	}

	@Transactional
	public Map<String, Object> getGroupMemberLocations(ResponseGroupMemberLocationDTO dto) {
		Map<String, Object> map = new HashMap<>();
		// REVIEW: 그룹 존재 여부, 조회 권한 확인 로직 추가 가능
		List<ResponseGroupMemberLocationDTO> locationList = dao.findGroupMemberLocations(dto);
		map.put("result", locationList);
		return map;
	}
	// GroupsService.java

    /**
     * 💡 [추가/수정] 그룹 삭제 또는 탈퇴
     * - 로그인한 사용자가 그룹장이면 그룹 전체 삭제 (deleteGroupAsLeader)
     * - 로그인한 사용자가 멤버이면 본인만 탈퇴 (leaveGroupAsMember)
     */
    @Transactional
    public Map<String, Object> deleteOrLeaveGroup() {
        // 1. 현재 로그인한 사용자 번호 가져오기
        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) {
            return createErrorResponse("로그인이 필요합니다.");
        }

        // 2. 현재 사용자가 보고 있는 그룹 ID(group_number) 찾기
        // (싱글 그룹 정책이므로, 사용자의 첫 번째 그룹 ID를 가져옵니다)
        List<ResponseGroupListItemDTO> groups = dao.findGroupsByUserNumber(userNumber);
        if (groups.isEmpty()) {
            return createErrorResponse("소속된 그룹이 없습니다.");
        }
        
        int groupId = groups.get(0).getId(); // 사용자의 group_number PK

        // 3. 이 그룹의 리더가 누구인지 확인
        Integer groupLeaderNumber = dao.findGroupLeaderByGroupId(groupId);
        if (groupLeaderNumber == null) {
            return createErrorResponse("그룹 정보를 찾을 수 없습니다.");
        }

        // 4. [핵심] 리더와 본인 비교
        if (userNumber.equals(groupLeaderNumber)) {
            // 시나리오 1: 내가 그룹장이다 -> 그룹 전체 삭제
            dao.deleteGroupAsLeader(groupId);
        } else {
            // 시나리오 2: 내가 그룹원이다 -> 나만 탈퇴
            Map<String, Object> map = new HashMap<>();
            map.put("groupId", groupId); // 나의 group_number(PK)
            map.put("userNumber", userNumber); // 나의 userNumber
            dao.leaveGroupAsMember(map);
        }

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        return successResponse;
    }
	/**
	 * [헬퍼 메서드]
	 * 세션에서 현재 로그인된 사용자의 고유번호(userNumber)를 가져옵니다.
	 * (이 메서드가 서비스 클래스 내에 이미 있는지 확인하세요.)
	 * * @return 로그인된 사용자의 userNumber, 비로그인 시 null
	 */
	// GroupsService.java 내의 getLoggedInUserNumber() 헬퍼 메서드
	private Integer getLoggedInUserNumber() {
	    Object userNumberObj = session.getAttribute("userNumber");
	    if (userNumberObj == null) {
	        return null; // ⬅️ 세션에 값이 없으면 안전하게 null 반환
	    }
	    try {
	        // ⬅️ (Integer)로 안전하게 형변환
	        return (Integer) userNumberObj; 
	    } catch (ClassCastException e) {
	        session.invalidate(); // 세션 데이터 손상 시 초기화
	        return null;
	    }
	}
	/**
	 * 공통: 에러 응답 Map 생성
	 */
	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> map = new HashMap<>();
		map.put("success", false);
		map.put("message", message);
		return map;
	}
	
	/**
	 * 초대 수락/거절 전, 초대의 유효성과 권한을 검사합니다.
	 * @param userNumber 현재 로그인한 사용자 번호 (수락/거절 권한자)
	 * @param invitationId 처리할 초대 ID
	 * @return 유효성 검사를 통과한 InvitationDTO 객체
	 * @throws RuntimeException 유효성 검사에 실패한 경우 (메시지 포함)
	 */
	private InvitationDTO validateAndGetPendingInvitation(Integer userNumber, Integer invitationId) {
		if (userNumber == null) {
			throw new RuntimeException("로그인이 필요합니다.");
		}
		if (invitationId == null) {
			throw new RuntimeException("초대 ID가 누락되었습니다.");
		}

		InvitationDTO invitation = dao.findInvitationById(invitationId);

		if (invitation == null) {
			throw new RuntimeException("존재하지 않는 초대입니다.");
		}
		
		// 보안: 본인에게 온 초대인지 확인
		if (!invitation.getTargetUserNumber().equals(userNumber)) {
			throw new RuntimeException("본인에게 온 초대가 아닙니다.");
		}
		
		// 상태: 처리 대기 중인지 확인 (Mapper의 findInvitationById가 'N' 상태만 조회)
		if (!"PENDING".equals(invitation.getStatus())) {
			throw new RuntimeException("이미 처리된 초대입니다.");
		}

		return invitation;
	}

	

}