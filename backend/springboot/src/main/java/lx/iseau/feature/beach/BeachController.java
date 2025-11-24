package lx.iseau.feature.beach;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/beach")
@RestController
@RequiredArgsConstructor
public class BeachController {	
	

	private final BeachService service;

	@RequestMapping("/beaches") // 나중에 코드 최적화 해야함, 컨트롤러단은 비즈니스로직 뺴기
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

	    Map<String, Object> res = service.getBeachList(request);
	    
	    // 정렬값도 함께 내려주고 싶으면(선택) 새 Map에 합쳐서 반환
	    Map<String, Object> body = new HashMap<>(res);
	    body.put("sort", request.getSort());

	    return ResponseEntity.ok()
	            .header("api", "beach/beachs")
	            .body(body);
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
	@RequestMapping("/detail/{beachNumber}/comments")
	public ResponseEntity<?> getBeachComments(@PathVariable int beachNumber) {
		Map<String, Object> result = service.getBeachComments(beachNumber);
	    return ResponseEntity.ok()
	        .body(Map.of("data", result));
	}
	
	// 해수욕장 리뷰 등록
	@RequestMapping("/detail/{beachNumber}/comments/insert")
	public ResponseEntity<?> insertBeachComment(@PathVariable int beachNumber, @RequestBody ResponseBeachCommentDTO dto) {
		dto.setBeachNumber(beachNumber);
		Map<String, Object> result = service.insertBeachComment(dto);
	    return ResponseEntity
	    		.ok()
		        .body(Map.of("data", result));
	}

	// 해수욕장 리뷰 수정
	@RequestMapping("/detail/{beachNumber}/comments/update/{beachCommentNumber}")
	public ResponseEntity<?> updateBeachComment(@PathVariable int beachNumber, @PathVariable int beachCommentNumber,
			@RequestBody ResponseBeachCommentDTO dto) {
		dto.setBeachNumber(beachNumber);
		dto.setBeachCommentNumber(beachCommentNumber);
		Map<String, Object> result = service.updateBeachComment(dto);
	    return ResponseEntity
	    		.ok()
		        .body(Map.of("data", result));
	}
	
	// 해수욕장 리뷰 삭제
	@RequestMapping("/detail/comments/delete/{beachCommentNumber}")
	public ResponseEntity<?> deleteBeachComment(@PathVariable int beachCommentNumber, @RequestBody ResponseBeachCommentDTO dto) {
		dto.setBeachCommentNumber(beachCommentNumber);
		Map<String, Object> result = service.deleteBeachComment(dto);
		return ResponseEntity
				.ok()
				.body(Map.of("data", result));
	}
	
	// 해수욕장 내 리뷰 목록 조회
	@GetMapping("/comments/my")
	public ResponseEntity<?> getMyBeachComments(
	        @RequestParam(name = "sort", defaultValue = "latest") String sort
	) {
	    Map<String, Object> result = service.getMyBeachComments(sort);
	    return ResponseEntity.ok()
	            .header("api", "Beach/comments/my")
	            .body(Map.of("data", result)); // result 안에 userNumber도 담아서 리턴
	}

	/* 해수욕장 리뷰 수정 (세션말고 토큰할때)
	@PutMapping("/detail/{beachNumber}/comments/{beachCommentNumber}")
	public ResponseEntity<?> updateBeachComment(@PathVariable int beachNumber,
	                                       @PathVariable("beachCommentNumber") int beachCommentNumber,
	                                       //@AuthenticationPrincipal user, 로그인 후 추가
	                                       @RequestBody ResponseBeachCommentDTO dto) {
	    // 임시 사용자 (로그인 기능 후 @AuthenticationPrincipal 교체)
	    dto.setBeachNumber(beachNumber);
	    dto.setBeachCommentNumber(beachCommentNumber);

	    int updated = service.updateBeachComment(dto);
	    return ResponseEntity.ok(Map.of("success", updated > 0));
	}
	 */
	
	

	@GetMapping("/favorites/my")
	public ResponseEntity<?> getBeachFavorites() {
	    Map<String, Object> favoritesMap = service.getBeachFavorites();
	    // result 안에 이미 List<Integer>가 들어 있음
	    return ResponseEntity
	            .ok()
	            .header("api", "Beach/beachs/favorites")
	            .body(Map.of("data", favoritesMap));
	}
	
	@RequestMapping("/favorites")
	public ResponseEntity<?> insertFavorite(@RequestBody ResponseFavoritesDTO dto) {
	
        // 2. DTO에서 beachNumber 가져오기
        int beachNumber = dto.getBeachNumber();
        try {
            // 3. 서비스 호출
            int result = service.insertFavorite(beachNumber);

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

        try {
     
            int result = service.removeFavorite(beachNumber);

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
	@PutMapping("/user/select-beach/{beachNumber}")
	public ResponseEntity<?> updateUserBeachSelection(
	        @PathVariable int beachNumber,
	        @RequestBody Map<String, Integer> body // 🚨 body에서 userNumber 받기
	) {
	    // body에서 userNumber가 있으면 쓰고, 없으면 1(기본값) 사용
	    int userNumber = body.getOrDefault("userNumber", 1);
	    
	    try {
	        service.saveUserBeach(userNumber, beachNumber);
	        return ResponseEntity.ok(Map.of("success", true, "userNumber", userNumber));
	    } catch (Exception e) {
	        return ResponseEntity.status(500).body(Map.of("error", "오류 발생"));
	    }
	}
} // 클래스 끝