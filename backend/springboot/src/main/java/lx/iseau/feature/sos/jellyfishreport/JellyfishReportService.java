package lx.iseau.feature.sos.jellyfishreport;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
public class JellyfishReportService {
	
	@Autowired
	JellyfishReportDAO dao;
	@Autowired
    FileStorageService fileStorageService;


    @Transactional
    public Integer createReport(JellyfishReportDTO dto, MultipartFile imageFile) {
        // 파일 저장 → 접근 가능한 URL 반환 (예: "/files/2025/10/xxx.jpg")
        String savedUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            savedUrl = fileStorageService.store(imageFile);
        }
        dto.setImageUrl(savedUrl);                 // DB에는 URL만 저장
        
        // ✅ 파일이 없으면 빈 문자열로 보정하여 DB NOT NULL/제약을 회피
        if (dto.getImageUrl() == null) dto.setImageUrl("");

        if (dto.getAdminApproval() == null || dto.getAdminApproval().isBlank()) {
            dto.setAdminApproval("N");            // 기본값 보완(안전)
        }

        dao.insertReport(dto);      // useGeneratedKeys로 reportNumber 채워짐
        return dto.getReportNumber();
    }
}