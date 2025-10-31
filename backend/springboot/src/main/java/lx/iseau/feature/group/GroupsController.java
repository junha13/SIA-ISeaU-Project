package lx.iseau.feature.group;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestAttribute; // 임시 제거
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lx.iseau.feature.group.RequestGroupInviteDTO;
import lx.iseau.feature.group.ResponseGroupListItemDTO;
import lx.iseau.feature.group.ResponseGroupMemberLocationDTO;
import lx.iseau.feature.group.RequestLocationShare;
import lx.iseau.feature.post.PostDAO;

@RequestMapping("/api/groups")
@RestController
@RequiredArgsConstructor
public class GroupsController {

    private final GroupsService service;


    /**
     * 그룹 만들기
     */
    @RequestMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody RequestGroupDTO dto) { // @RequestAttribute 임시 제거
        // 임시 사용자 ID 사용
        Map<String, Object> result = service.createGroup(dto);
        return ResponseEntity
                .ok()
                .header("api", "Groups/create")
                .body(Map.of("data", result));
    }

    /**
     * 그룹 더블체크
     */
    @RequestMapping("/doubleCheck")
    public ResponseEntity<?> doubleCheckGroupName(@RequestBody RequestGroupDTO dto) { // @RequestAttribute 임시 제거
        // 임시 사용자 ID 사용
        Map<String, Object> result = service.doubleCheckGroupName(dto);
        return ResponseEntity
                .ok()
                .header("api", "Groups/doubleCheck")
                .body(Map.of("data", result));
    }

    /**
     * 그룹 목록 조회 (GET /api/groups)
     */
    @RequestMapping
    public ResponseEntity<?> getGroupsList() {
    	Map<String, Object> result = service.getGroupsList();
        //   1. 서비스가 "로그인이 필요합니다"라는 메시지를 반환했는지 확인
        if (result.get("message") != null && result.get("message").toString().contains("로그인")) {
             // 500 서버 다운 대신, "401 권한 없음" 에러를 프론트에 반환
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity
                .ok()
                .header("api", "Groups/getGroupsList")
                .body(Map.of("data", result));
    }
    /**
     * 그룹원 초대 (POST /api/groups/invite)
     */
    @RequestMapping("/invite") //
    public ResponseEntity<?> inviteGroupMember(@RequestBody RequestGroupInviteDTO request) {
        Map<String, Object> result = service.inviteGroupMember(request);

        // 1.서비스가 반환한 결과에서 로그인 에러인지 확인
        if (Boolean.FALSE.equals(result.get("success")) &&
            result.get("message") != null &&
            result.get("message").toString().contains("로그인")) {
             // 2. 로그인 필요 시 401 반환 (서비스 결과를 body에 포함)
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity
                .ok()
                .header("api", "Groups/invite")
                .body(Map.of("data", result));
    }

    /**
     * 위치 공유 수락 (POST /api/groups/location/accept)
     * (수락자 정보를 세션에서 가져오도록 수정)
     */
    @RequestMapping("/location/accept") //
    public ResponseEntity<?> acceptLocationShare(@RequestBody RequestLocationShare request) {

        Map<String, Object> result = service.acceptLocationShare(request);

        return ResponseEntity
                .ok()
                .header("api", "Groups/location/accept")
                .body(Map.of("data", result));
    }
    /**
     *
     * 위치 공유 거절 (POST /api/groups/location/reject)
     */
    @RequestMapping("/location/reject")
    public ResponseEntity<?> rejectLocationShare(@RequestBody RequestLocationShare request) {

    	Map<String, Object> result = service.rejectLocationShare(request);

        return ResponseEntity
                .ok()
                .header("api", "Groups/location/reject")
                .body(Map.of("data", result));
    }
    /**
     * 그룹 멤버 위치 조회 (GET /api/groups/locations)
     * 이 API는 원래 userId가 필요 없었으므로 변경 없음
     */
    @RequestMapping("/locations")
    public ResponseEntity<?> getGroupMemberLocations(
            @RequestParam("groupId") int groupId) {
        Map<String, Object> result = service.getGroupMemberLocations(groupId);
        return ResponseEntity
                .ok()
                .header("api", "Groups/locations")
                .body(Map.of("data", result));
    }
    @RequestMapping("/invitations/pending")
    public ResponseEntity<?> getPendingInvitations() {
        Map<String, Object> result = service.getPendingInvitations();

        return ResponseEntity
                .ok()
                .header("api", "Groups/invitations/pending")
                .body(Map.of("data", result));
    }
    @RequestMapping("/{id}") 
    public ResponseEntity<?> deleteGroup(@PathVariable("id") int id) {
        // @PathVariable("id")는 받지만, 
        // 서비스는 세션 ID를 기준으로만 동작하게 합니다.        
        Map<String, Object> result = service.deleteOrLeaveGroup();
        return ResponseEntity
                .ok()
                .header("api", "Groups/deleteGroup") // ⬅️ 헤더 추가
                .body(Map.of("data", result));
    }
    
}
