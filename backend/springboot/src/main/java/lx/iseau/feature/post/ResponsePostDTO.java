package lx.iseau.feature.post;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePostDTO {
    
    
	private int postNumber;
	private String postTitle;
	private String postContent;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm") // 응답을 분까지만 보이게
	private LocalDateTime createdAt;
	
	private int userNumber;
	private int viewCount;

    // board 컨디션
    private String boardName;
    
    // user name (활동명)
    private String id;
    
    // 추천
    private int recommendCount;    // 총 추천 수
    private boolean recommendedByMe; // 내가 추천했는지 (viewer 기준)
    
    // 댓글 내용
    private String commentContent;
}