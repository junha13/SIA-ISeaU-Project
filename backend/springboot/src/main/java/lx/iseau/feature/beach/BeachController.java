package lx.iseau.feature.beach;

// 1. 필요한 클래스들을 import 합니다.
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lx.iseau.feature.beach.BeachDTO;
import lx.iseau.feature.beach.BeachListRequest;
import lx.iseau.feature.beach.BeachService;

@RequestMapping("/api/beach")
@RestController
public class BeachController {	
	
	@Autowired
	private BeachService beachService; 

	@RequestMapping("/beaches")
	public ResponseEntity<?> getBeachList(@RequestBody BeachListRequest request) {
		
       
		List<BeachDTO> beachList = beachService.getBeachList(request);
		
        
		return ResponseEntity.ok()
					.header("api", "beach/beachs")
					.body(Map.of("result", beachList));
	}
 
}