package lx.iseau.feature.beach;

// 1. 필요한 클래스들을 import 합니다.
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lx.iseau.feature.beach.ResponseBeachDTO;
import lx.iseau.feature.beach.BeachListRequest;
import lx.iseau.feature.beach.BeachService;

@RequestMapping("/api/beach")
@RestController
public class BeachController {	
	
	@Autowired
	private BeachService service; 

	@RequestMapping("/beaches")
	public ResponseEntity<?> getBeachList(@RequestBody BeachListRequest request) {
		List<ResponseBeachDTO> beachList = service.getBeachList(request);
		return ResponseEntity.ok()
					.header("api", "beach/beachs")
					.body(Map.of("result", beachList));
	}
	
	
	/*
	 * ========= 하나의 해수욕장의 값을 보내주면 딤 =========
	 */
	@RequestMapping("/detail/{beachNumber}/info")
	public ResponseEntity<?> getBeachDetailInfo(@PathVariable int beachNumber) {
		Map<String, Object> result = service.getBeachDetailInfo(beachNumber);
		return ResponseEntity
				.ok()
				.header("api", "Beach/detail/info")
				.body(Map.of("data", result));
	}
	
	/*
	 * ========= 하나의 해수욕장에 대한 여러 시간대 danget 상황 보내주기 =========
	 */
	@RequestMapping("/detail/{beachNumber}/danger")
	public ResponseEntity<?> getBeachDetailDanger(@PathVariable int beachNumber) {
		Map<String, Object> result = service.getBeachDetailDanger(beachNumber);
		return ResponseEntity
				.ok()
				.header("api", "Beach/detail/danger")
				.body(Map.of("data", result));
	}
 
}