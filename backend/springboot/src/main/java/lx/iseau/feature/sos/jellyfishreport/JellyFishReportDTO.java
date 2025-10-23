package lx.iseau.feature.sos.jellyfishreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class JellyFishReportDTO {
	private int reportNumber;
	
	private Double lat;
	private Double lon;
	
	private String mobile;
    private String details;
    private String imageUrl;
    private String adminApproval;
}
