package lx.iseau.feature.beach;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseFavoritesDTO {
    private Integer beachNumber; // 프론트에서 보내는 최소 정보
}