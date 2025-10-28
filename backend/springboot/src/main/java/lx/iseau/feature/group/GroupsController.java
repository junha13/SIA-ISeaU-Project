package lx.iseau.feature.group;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestAttribute; // 임시 제거
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lx.iseau.feature.group.GroupInviteRequestDTO;
import lx.iseau.feature.group.GroupListItemResponseDTO;
import lx.iseau.feature.group.GroupMemberLocationResponseDTO;
import lx.iseau.feature.group.LocationShareRequest;

@RequestMapping("/api/groups")
@RestController
public class GroupsController {

    @Autowired
    GroupsService service;

    // 임시 사용자 ID (테스트용)
    private static final String TEMP_USER_ID = "tempUser";

    /**
     * 그룹 목록 조회 (GET /api/groups) - 임시: 인증 없이
     */
    @RequestMapping
    public ResponseEntity<?> getGroupsList(
            /* @RequestAttribute("userId") String userId */) { // @RequestAttribute 임시 제거
        // 임시 사용자 ID 사용
        Map<String, Object> result = service.getGroupsList(TEMP_USER_ID);
        return ResponseEntity
                .ok()
                .header("api", "Groups/getGroupsList")
                .body(Map.of("data", result));
    }

    /**
     * 그룹원 초대 (POST /api/groups/invite) - 임시: 인증 없이
     */
    @RequestMapping("/invite")
    public ResponseEntity<?> inviteGroupMember(
            @RequestBody GroupInviteRequestDTO request
            /*, @RequestAttribute("userId") String inviterUserId */) { // @RequestAttribute 임시 제거
        // 임시 사용자 ID를 초대자로 사용
        Map<String, Object> result = service.inviteGroupMember(request, TEMP_USER_ID);
        return ResponseEntity
                .ok()
                .header("api", "Groups/invite")
                .body(Map.of("data", result));
    }

    /**
     * 위치 공유 수락 (POST /api/groups/location/accept) - 임시: 인증 없이
     */
    @RequestMapping("/location/accept")
    public ResponseEntity<?> acceptLocationShare(
            @RequestBody LocationShareRequest request
            /*, @RequestAttribute("userId") String userId */) { // @RequestAttribute 임시 제거
        // 임시 사용자 ID를 수락자로 사용
        Map<String, Object> result = service.acceptLocationShare(request, TEMP_USER_ID);
        return ResponseEntity
                .ok()
                .header("api", "Groups/location/accept")
                .body(Map.of("data", result));
    }

    /**
     * 위치 공유 거절 (POST /api/groups/location/reject) - 임시: 인증 없이
     */
    @RequestMapping("/location/reject")
    public ResponseEntity<?> rejectLocationShare(
            @RequestBody LocationShareRequest request
            /*, @RequestAttribute("userId") String userId */) { // @RequestAttribute 임시 제거
        // 임시 사용자 ID를 거절자로 사용
        Map<String, Object> result = service.rejectLocationShare(request, TEMP_USER_ID);
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
}