package lx.iseau.feature.group;

import lx.iseau.feature.group.RequestGroupInviteDTO;
import lx.iseau.feature.group.ResponseGroupListItemDTO;
import lx.iseau.feature.group.ResponseGroupMemberLocationDTO;
import lx.iseau.feature.group.InvitationDTO;
import lx.iseau.feature.group.RequestLocationShare;
import lx.iseau.feature.group.ResponseGroupDTO;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface GroupsDAO {

	// --- 그룹 생성 ---
	int insertGroup(RequestGroupDTO dto); // (리더를 'Y' 상태로 추가)
	int SelectdoubleCheckByGroupName(RequestGroupDTO dto);

	// --- (A) 그룹 목록 조회 ---

	Integer findUserNumberById(String userId);
	List<ResponseGroupListItemDTO> findGroupsByUserNumber(int userNumber);

	// --- (B) 그룹원 초대 (tb_group에 'N'으로 INSERT) ---

	int insertInvitation(RequestGroupInviteDTO request);

	Integer findGroupLeaderByGroupId(Integer groupId);

	List<InvitationDTO> findPendingInvitationsByMember(Integer userNumber);
	// --- (C) 위치 공유 수락/거절 (tb_group을 UPDATE 또는 DELETE) ---

	InvitationDTO findInvitationById(int invitationId);


	int updateInviteStatus(Map<String, Object> statusParams);


	// --- (D) 그룹 위치 조회 (tb_group에서 'Y'인 멤버만) ---
	List<ResponseGroupMemberLocationDTO> findGroupMemberLocations(int groupId);
}