package lx.iseau.feature.beach;

import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor 
@AllArgsConstructor
public class BeachListRequest {
    
    private String region;     
    private String sort;
    private String category;
    private Integer page;      // 1부터
    private Integer size;      // 10까지
    private String tagFilter;
}