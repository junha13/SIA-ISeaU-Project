package lx.iseau.feature.controltower;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 비즈니스 규칙(최소):
 * - 삭제 전 참조(tb_beach, tb_manager) 존재 시 삭제 금지 (친절한 메시지)
 * - 매니저 추가 시: managerUserNumber는 tb_user.user_number가 존재해야 함(미존재 시 DB FK 에러)
 */
@Service
public class ControlTowerService {

    private final ControlTowerDAO dao;

    public ControlTowerService(ControlTowerDAO dao) {
        this.dao = dao;
    }

    /** 목록 조회 */
    public List<ControlTowerDTO> list() {
        return dao.selectControlTowers();
    }

    /** 단건 조회 */
    public ControlTowerDTO get(Integer id) {
        return dao.selectControlTowerById(id);
    }

    /** 생성 */
    @Transactional
    public Integer create(String name) {
        dao.insertControlTower(name);
        // PostgreSQL useGeneratedKeys(true)로 PK를 DTO로 받지 않고, 방금생성 ID를 재조회하는 단순 방식
        // (가벼운 환경에서는 가장 큰 PK를 찾는 방식보다 select max 대신 order desc limit 1이 안전)
        List<ControlTowerDTO> list = dao.selectControlTowers();
        return list.isEmpty() ? null : list.get(list.size() - 1).getControlTowerNumber();
    }

    /** 이름 변경 */
    @Transactional
    public void rename(Integer id, String name) {
        dao.updateControlTowerName(id, name);
    }

    /** 삭제(참조 존재하면 금지) */
    @Transactional
    public void delete(Integer id) {
        int beachRefs = dao.countBeachesUsingTower(id);
        int managerRefs = dao.countManagersUsingTower(id);
        if (beachRefs > 0 || managerRefs > 0) {
            throw new IllegalStateException(
                String.format("삭제 불가: 해변 참조 %d건, 매니저 참조 %d건이 남아있습니다.", beachRefs, managerRefs)
            );
        }
        dao.deleteControlTower(id);
    }

    /** 매니저 목록 */
    public List<ManagerSummaryDTO> listManagers(Integer towerId) {
        return dao.selectManagersByTower(towerId);
    }

    /** 매니저 배정 */
    @Transactional
    public void addManager(Integer towerId, Integer managerUserNumber) {
        dao.insertManagerToTower(towerId, managerUserNumber);
    }

    /** 매니저 해제 */
    @Transactional
    public void removeManager(Integer towerId, Integer managerUserNumber) {
        dao.deleteManagerFromTower(towerId, managerUserNumber);
    }
}