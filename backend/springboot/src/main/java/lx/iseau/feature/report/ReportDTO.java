package lx.iseau.feature.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {
	private int firstAidNumber; 
	private String firstAidCase;
    private String firstAidContent;
    private String firstAidStep;
    private String firstAidImage;
    private String firstAidDescription;
}
