package lx.iseau.feature.post;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostDAO {
	
	public int insertPostDB(PostVO post);

	public int selectBoardNumber(BoardVO board);

	public List<ResponsePostDTO> selectPostList();
	
	public ResponsePostDTO selectPostDetailByPostId(int PostNumber);

	public int updateIncrementViewCount(int PostNumber);

	public int selectUserNumberByUserLoginId(String userLoginId);

	public boolean selectPostRecommendByMe(RequestPostRecommendDTO recommendDto);

	public int insertPostRecommend(RequestPostRecommendDTO recommendDto);

	public int deletePostRecommend(RequestPostRecommendDTO recommendDto);

	public int selectRecommendCount(RequestPostRecommendDTO recommendDto);
	
	public int insertPostCommentDB(RequestPostCommentDTO commentDto);
	
	public List<RequestPostCommentDTO> selectCommentList(int postNumber);
}