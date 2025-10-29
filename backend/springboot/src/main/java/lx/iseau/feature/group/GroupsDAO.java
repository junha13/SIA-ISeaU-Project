package lx.iseau.feature.group;

import lx.iseau.feature.group.GroupInviteRequestDTO;
import lx.iseau.feature.group.GroupListItemResponseDTO;
import lx.iseau.feature.group.GroupMemberLocationResponseDTO;
import lx.iseau.feature.group.InvitationDTO;
import lx.iseau.feature.group.LocationShareRequest;
import lx.iseau.feature.group.ResponseGroupDTO;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface GroupsDAO {

	int insertGroup(RequestGroupDTO dto);
	
	int SelectdoubleCheckByGroupName(RequestGroupDTO dto);
	
    // --- (A) 그룹 목록 조회: GET /groups ---
	
    Integer findUserNumberById(String userId);

    List<GroupListItemResponseDTO> findGroupsByUserNumber(int userNumber);


    // --- (B) 그룹원 초대: POST /groups/invite ---

    int insertInvitation(GroupInviteRequestDTO request);


    // --- (C) 위치 공유 수락/거절: POST /groups/location/accept & /reject ---

    InvitationDTO findInvitationById(int invitationId); // REVIEW: InvitationDTO 클래스 및 DAO 구현 필요

    int updateInviteStatus(Map<String, Object> statusParams);

    int insertGroupMember(ResponseGroupDTO groupDto);


    // --- (D) 그룹 위치 조회: GET /groups/locations ---

    List<GroupMemberLocationResponseDTO> findGroupMemberLocations(int groupId);
}