package lx.iseau.feature.location;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lx.iseau.feature.group.GroupsService;

@RequestMapping("/api/location")
@RestController
@RequiredArgsConstructor
public class LocationController {

	
    private final LocationService service;
    
    /**
     * 내위치와 바운더리 비교
     */
    @RequestMapping("/boundaryCheck")
    public ResponseEntity<?> boundaryCheck(@RequestBody RequestUserLocationDTO dto) { // @RequestAttribute 임시 제거
        Map<String, Object> result = service.boundaryCheck(dto);
        return ResponseEntity
                .ok()
                .header("api", "location/boundaryCheck")
                .body(Map.of("data", result));
    }
    
    /**
     * 내위치와 바운더리 비교
     */
    @RequestMapping("/testBoundaryCheck")
    public ResponseEntity<?> BoundaryCheck(@RequestBody RequestUserLocationDTO dto) { // @RequestAttribute 임시 제거
    	Map<String, Object> result = service.testBoundaryCheck(dto);
    	return ResponseEntity
    			.ok()
    			.header("api", "location/testBoundaryCheck")
    			.body(Map.of("data", result));
    }

}
