package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class GroupListItemResponseDTO {

    private Integer id;
    private String name;
    private Integer memberCount;
}