package lx.iseau.feature.beach;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBeachCommentDTO {
    private int beachCommentNumber;
    private String commentContent;
    private LocalDateTime createdTime;
    private int rating;
    private int beachNumber;
    private int userNumber;
}