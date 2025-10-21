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
}