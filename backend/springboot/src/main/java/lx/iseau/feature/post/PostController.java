package lx.iseau.feature.post;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/post")
@RestController
@RequiredArgsConstructor
public class PostController {
	


	private final PostService service;

	// ========== 게시글 db insert ==========
	@RequestMapping("/post")
	public ResponseEntity<?> insertPostDB(@RequestBody RequestPostDTO dto) {
		Map<String, Object> result = service.insertPostDB(dto);
		return ResponseEntity
				.ok()
				.header("api", "post/post")
				.body(Map.of("result", result));
	}

	// ========== postListview 초기 onmounted list 보여주기 ==========
	@RequestMapping("/getPostList")
	public ResponseEntity<?> selectPostList() {
		List<ResponsePostDTO> list = service.selectPostList();
		return ResponseEntity
				.ok()
				.header("api", "post/getPostList")
				.body(Map.of("result", list));
	}

	// ========== 각 게시글 디테일 화면 보여주기 ==========
	@RequestMapping("/getPostDetail/{postNumber}")
	public ResponseEntity<?> selectPostDetail(@PathVariable int postNumber) {
		Map<String, Object> map = service.selectPostDetailByPostId(postNumber);
		return ResponseEntity
				.ok()
				.header("api", "post/getPostDetail" + postNumber)
				.body(Map.of("result", map));
	}

	// ========== 게시글 추천 api  ==========
	@RequestMapping("/recommend")
	public ResponseEntity<?> insertPostRecommend(@RequestBody RequestPostRecommendDTO dto) {
		Map<String, Object> map = service.insertPostRecommend(dto);
		return ResponseEntity
				.ok()
				.header("api", "post/recommend")
				.body(Map.of("result", map));
	}

	// ========== 게시글 댓글 추가 api ==========
	@RequestMapping("/addComment")
	public ResponseEntity<?> insertPostCommentDB(@RequestBody RequestPostCommentDTO dto) {
		Map<String, Object> map = service.insertPostCommentDB(dto);
		return ResponseEntity
				.ok()
				.header("api", "post/addComment")
				.body(Map.of("result", map));
	}
}
