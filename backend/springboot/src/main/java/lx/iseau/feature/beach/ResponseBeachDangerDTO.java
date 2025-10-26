package lx.iseau.feature.beach;

import java.sql.Date;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBeachDangerDTO {
	private double waveHeight;
	private double seaSurfaceTemperature;
	private double lat;
    private double latitude;
    private double longitude;
   
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm") // 응답을 분까지만 보이게
	private Date forecastTime;
	
}