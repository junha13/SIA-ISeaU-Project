package lx.iseau.feature.sos.jellyfishreport;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
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
			@RequestParam("lat") Double lat,
			@RequestParam("lon") Double lon,
			@RequestParam(value = "details", required = false) String details,
			@RequestParam(value = "mobile", required = false) String mobile,
			@RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
			@RequestParam(value = "adminApproval", required = false) String adminApproval
	) {
		try {
			
			if (lat == null || lon == null) {
				return ResponseEntity.badRequest()
						.body(Map.of("error", "BadRequest", "message", "위치 좌표(lat/lon)는 필수입니다."));
			}

			// DTO 구성 (imageUrl은 서비스에서 세팅)
			JellyfishReportDTO dto = new JellyfishReportDTO();
			dto.setLat(lat);
			dto.setLon(lon);
			dto.setDetails(details);
			dto.setMobile(mobile);
			dto.setAdminApproval((adminApproval == null || adminApproval.isBlank()) ? "N" : adminApproval);

			Integer id = service.createReport(dto, imageFile);
			
            // ✅ HashMap은 null value 허용
            Map<String, Object> body = new HashMap<>();
            body.put("reportNumber", id);
            body.put("imageUrl", dto.getImageUrl()); // null이어도 OK

			return ResponseEntity.created(URI.create("/api/sos/jellyfish-report/" + id))
					.body(Map.of("reportNumber", id, "imageUrl", dto.getImageUrl()));
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body(Map.of("error", e.getClass().getSimpleName(), "message", e.getMessage()));
		}
	}

}
