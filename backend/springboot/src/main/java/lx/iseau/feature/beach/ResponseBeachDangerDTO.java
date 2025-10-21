package lx.iseau.feature.beach;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBeachDangerDTO {
	private int weather;
	private int temperature;
	private int wind_speed;
	private String wind_direction;
	private String strong_wind;
	private String rain_fall_percent;
	private String rainfall;
	private int uv;
	private String wave_height;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm") // 응답을 분까지만 보이게
	private Date forecastTime;
	
	private double lat;
	private double lon;
}