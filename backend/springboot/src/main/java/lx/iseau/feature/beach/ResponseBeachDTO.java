package lx.iseau.feature.beach;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBeachDTO {
	private int beachNumber;
	private String beachName;
    private String beachImage;
    private String beachInformation; 
    private double rating;
    private boolean isApprovedByMinistry;
    private String address;
    private double latitude;
    private double longitude; 
    private String mobile;
    private LocalDate openDate;
    private LocalDate closeDate;
    // ✅ 추가
    private int reviewCount; 
    private String tagsString;
}