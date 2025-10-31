package lx.iseau.feature.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPostDTO {
	
	private String postTitle;
	private String postContent;
	
//	private int boardNumber;
    private int userNumber;
	
    // board 컨디션
    //private String regionName;
    //private String boardCategoryName;
    private String boardName;
    
    // user name (활동명)
    private String userName;
}