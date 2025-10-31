package lx.iseau.feature.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardVO {
	private int boardNumber;
	//private String regionName;
	//private String boardCategoryName;
	private String boardName;

	
	public BoardVO(String boardName) {
		this.boardName = boardName;
	}
}