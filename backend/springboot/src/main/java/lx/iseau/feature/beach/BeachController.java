package lx.iseau.feature.beach;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/beach")
@RestController
public class BeachController {	
	
	@Autowired
	private BeachService service; 

	@RequestMapping("/beaches")
	public ResponseEntity<?> getBeachList(@RequestBody BeachListRequest request) {
	    String sort = (request.getSort() != null) ? request.getSort().toLowerCase() : "";

	    switch (sort) {
	        case "rating_desc":    // 평점순
	        case "review_desc":    // 리뷰 많은 순
	        case "name_asc":       // 이름순
	        case "distance_asc":   // 거리 가까운 순 (추후)
	            break; // 정상 sort 값 → 그대로 사용
	        default:
	            // 잘못된 값 들어오면 안전하게 rating_desc로 보정
	            request.setSort("name_asc");
	    }

	    List<ResponseBeachDTO> beachList = service.getBeachList(request);

	    return ResponseEntity.ok()
	            .header("api", "beach/beachs")
	            .body(Map.of(
	                "sort", request.getSort(),
	                "result", beachList
	            ));
	}
	
	@RequestMapping("/detail/{beachNumber}/info")
	public ResponseEntity<?> getBeachDetailInfo(@PathVariable int beachNumber) {
		Map<String, Object> result = service.getBeachDetailInfo(beachNumber);
		return ResponseEntity
				.ok()
				.header("api", "Beach/detail/info")
				.body(Map.of("data", result));
	}
	
	@RequestMapping("/detail/{beachNumber}/danger")
	public ResponseEntity<?> getBeachDetailDanger(@PathVariable int beachNumber) {
		Map<String, Object> result = service.getBeachDetailDanger(beachNumber);
		return ResponseEntity
				.ok()
				.header("api", "Beach/detail/danger")
				.body(Map.of("data", result));
	}

	@RequestMapping("/detail/{beachNumber}/weather")
	public ResponseEntity<?> getBeachDetailWeather(@PathVariable int beachNumber) {
		Map<String, Object> result = service.getBeachDetailWeather(beachNumber);
		return ResponseEntity
				.ok()
				.header("api", "Beach/detail/weather")
				.body(Map.of("data", result));
	}
	
	// 해수욕장 리뷰 목록 조회
	@GetMapping("/detail/{beachNumber}/comments")
	public ResponseEntity<?> getComments(@PathVariable int beachNumber) {
	    var list = service.getListBeachComments(beachNumber);
	    return ResponseEntity.ok()
	        .header("api", "Beach/detail/comments")
	        .body(Map.of("result", list));
	}
	
	// 해수욕장 리뷰 등록
	@PostMapping("/detail/{beachNumber}/comments")
	public ResponseEntity<?> addBeachComment(@PathVariable int beachNumber,
	                                    @RequestBody ResponseBeachCommentDTO dto) {
	    // 임시 사용자
	    dto.setUserNumber(1);
	    dto.setBeachNumber(beachNumber);
	    int inserted = service.addBeachComment(dto);
	    return ResponseEntity.ok(Map.of("success", inserted > 0));
	}

	// 해수욕장 리뷰 수정
	@PutMapping("/detail/{beachNumber}/comments/{beachCommentNumber}")
	public ResponseEntity<?> editBeachComment(@PathVariable int beachNumber,
	                                       @PathVariable("beachCommentNumber") int beachCommentNumber,
	                                       //@AuthenticationPrincipal user, 로그인 후 추가
	                                       @RequestBody ResponseBeachCommentDTO dto) {
	    // 임시 사용자 (로그인 기능 후 @AuthenticationPrincipal 교체)
	    dto.setUserNumber(1); // dto.setUserNumber(user.getUserNumber());
	    dto.setBeachNumber(beachNumber);
	    dto.setBeachCommentNumber(beachCommentNumber);

	    int updated = service.editBeachComment(dto);
	    return ResponseEntity.ok(Map.of("success", updated > 0));
	}

	// 해수욕장 리뷰 삭제
	@DeleteMapping("/detail/comments/{beachCommentNumber}")
	public ResponseEntity<?> removeBeachComment(@PathVariable("beachCommentNumber") int beachCommentNumber) {
	    int deleted = service.removeBeachComment(beachCommentNumber);
	    return ResponseEntity.ok(Map.of("success", deleted > 0));
	}
	

	@GetMapping("/favorites/my")
	public ResponseEntity<?> getBeachFavorites() {
	    int userNumber = 1; 
	    Map<String, Object> favoritesMap = service.getBeachFavorites(userNumber);
	    // result 안에 이미 List<Integer>가 들어 있음
	    return ResponseEntity
	            .ok()
	            .header("api", "Beach/beachs/favorites")
	            .body(Map.of("data", favoritesMap));
	}
	
	@RequestMapping("/favorites")
	public ResponseEntity<?> insertFavorite(@RequestBody ResponseFavoritesDTO dto) {
	    int userNumber = 1; // 테스트용
        // 2. DTO에서 beachNumber 가져오기
        int beachNumber = dto.getBeachNumber();
        try {
            // 3. 서비스 호출
            int result = service.insertFavorite(userNumber, beachNumber);

            // 4. 성공 응답 반환
            return ResponseEntity
                    .ok()
                    .header("api", "favorites")
                    .body(Map.of("success", result > 0));

        } catch (Exception e) {
            // 예외 처리
             return ResponseEntity
                     .status(409) 
                     .body(Map.of("error", "즐겨찾기 추가 중 오류 발생: " + e.getMessage()));
        }
    }
	@DeleteMapping("/favorites/{beachNumber}")
    public ResponseEntity<?> removeFavorite( @PathVariable int beachNumber
            // @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        int userNumber = 1; // 임시 userNumber

        try {
     
            int result = service.removeFavorite(userNumber, beachNumber);

            return ResponseEntity
                    .ok()
                    .header("api", "favorites/remove")
                    .body(Map.of("success", result > 0)); 

        } catch (Exception e) {
             return ResponseEntity
                     .status(500) 
                     .body(Map.of("error", "즐겨찾기 제거 중 오류 발생: " + e.getMessage()));
        }
    }

} // 클래스 끝
