package lx.iseau.feature.sos.jellyfishreport;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
public class JellyfishReportService {
	
	@Autowired
	JellyfishReportDAO dao;

    @Transactional
    public int createReport(JellyfishReportDTO dto) {
        if (dto.getLat() == null || dto.getLon() == null)
            throw new IllegalArgumentException("위도/경도는 필수입니다.");
        if (dto.getLat() < -90 || dto.getLat() > 90)
            throw new IllegalArgumentException("lat 범위는 -90~90 입니다.");
        if (dto.getLon() < -180 || dto.getLon() > 180)
            throw new IllegalArgumentException("lon 범위는 -180~180 입니다.");
        if (dto.getAdminApproval() == null)
            dto.setAdminApproval("N");

        Integer id = dao.insertReport(dto);     // ← RETURNING 값 받기
        return id;
    }
}
