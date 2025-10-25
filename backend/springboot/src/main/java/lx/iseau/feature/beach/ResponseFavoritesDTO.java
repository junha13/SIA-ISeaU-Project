package lx.iseau.feature.beach;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseFavoritesDTO {
    // 📢 NOT NULL이 아닌 경우 반드시 객체 타입(Integer, Double) 사용
	private int userNumber; // int -> Integer로 변경
	private int beachNumber; // int -> Integer로 변경
	private String beachName;
	private String beachImage;
	private String beachInformation;
	private Double rating; // double -> Double로 변경
	private Boolean isApprovedByMinistry; // boolean -> Boolean으로 변경 (NULL 방지)
	private String address;
	private Double latitude; // double -> Double로 변경
	private Double longitude; // double -> Double로 변경
   private String mobile;
   private LocalDate openDate;
   private LocalDate closeDate;
}
