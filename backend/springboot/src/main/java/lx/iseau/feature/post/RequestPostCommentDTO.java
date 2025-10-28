package lx.iseau.feature.post;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestPostCommentDTO {
	private int postNumber;
	private int userNumber;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm") // 응답을 분까지만 보이게
	private LocalDateTime createdAt;
	
    // user name (활동명)
    private String id;
    
    // 댓글 내용
    private String commentContent;
}
