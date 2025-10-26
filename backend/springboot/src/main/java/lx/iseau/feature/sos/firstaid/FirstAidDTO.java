package lx.iseau.feature.sos.firstaid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirstAidDTO {
	private int firstAidNumber; 
	private int firstAidCaseNum;
	private String firstAidCaseName;
    private String firstAidContent;
    private int firstAidStep;
    private String firstAidImage;
    private String firstAidDescription;
}
