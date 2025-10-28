package lx.iseau.feature.post;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestPostRecommendDTO {
	private int postNumber;
	private int userNumber;
    
    // 추천
    private int recommendCount;    // 총 추천 수
    private boolean recommendedByMe; // 내가 추천했는지 (viewer 기준)
}
