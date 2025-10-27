package lx.iseau.feature.sos.jellyfishreport;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RequestMapping("/api/sos")
@RestController
public class JellyfishReportController {
	@Autowired
	JellyfishReportService service;
	
	@PostMapping(value = "/jellyfish-report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
    		// 프론트에서 formData로 전송하기에 RequestPart로 받음
    		@RequestPart("lat") Double lat,
    		@RequestPart("lon") Double lon,
    		@RequestPart(value = "details", required = false) String details,
    		@RequestPart(value = "mobile", required = false) String mobile,
            @RequestPart(value = "imageUrl", required = false) MultipartFile imageUrl,
            @RequestPart(value = "adminApproval", required = false) String adminApproval
    ) {
        try {
            // 0) 기본 검증
            if (lat == null || lon == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "BadRequest", "message", "위치 좌표(lat/lon)는 필수입니다."));
            }
            if (imageUrl == null || imageUrl.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "BadRequest", "message", "이미지 파일은 필수입니다."));
            }

            // 1) 이미지 저장 (확장자 화이트리스트 + 디렉토리 분기)
            String original = imageUrl.getOriginalFilename();
            String ext = (original != null) ? StringUtils.getFilenameExtension(original).toLowerCase() : null;
            if (ext == null || !(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("webp") || ext.equals("heic"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "BadRequest", "message", "이미지 확장자는 jpg/jpeg/png/webp/heic 만 허용됩니다."));
            }

            LocalDate today = LocalDate.now();
            Path base = Path.of("uploads", "jellyfish",
                    String.valueOf(today.getYear()), String.format("%02d", today.getMonthValue()));
            Files.createDirectories(base);

            String filename = UUID.randomUUID() + "." + ext;
            Path target = base.resolve(filename);
            imageUrl.transferTo(target.toFile());

            // 정적 접근용 URL (아래 ResourceHandler와 매칭)
            String publicUrl = "/files/jellyfish/" + today.getYear() + "/"
                    + String.format("%02d", today.getMonthValue()) + "/" + filename;

            // 2) DTO 구성
            JellyfishReportDTO dto = new JellyfishReportDTO();
            dto.setLat(lat);
            dto.setLon(lon);
            dto.setDetails(details);
            dto.setMobile(mobile);
            dto.setImageUrl(publicUrl);           
            dto.setAdminApproval((adminApproval == null || adminApproval.isBlank()) ? "N" : adminApproval);

            // 3) 저장
            int id = service.createReport(dto);

            return ResponseEntity
                    .created(URI.create("/jellyfish-report/" + id))
                    .body(Map.of("reportNumber", id, "imageUrl", publicUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getClass().getSimpleName(),
                "message", e.getMessage()
            ));
        }
    }
	
	// 매핑 확인용
    @GetMapping("/jellyfish-report/_ping")
    public String ping() { return "ok"; }
}
