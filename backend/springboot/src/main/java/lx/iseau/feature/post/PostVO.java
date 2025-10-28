package lx.iseau.feature.post;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostVO {
	private int postNumber;
	private String postTitle;
	private String postContent;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm") // 응답을 분까지만 보이게
	private LocalDateTime createdAt;
	
	private int boardNumber;
	private int userNumber;
	
	private int viewCount;
	
	// DB 추가해야됨
	private String tags;
	
	PostVO(String title, String content, int boardNumber, int userNumber) {
		this.postTitle = title;
		this.postContent = content;
		this.boardNumber = boardNumber;
		this.userNumber = userNumber;
	}
}