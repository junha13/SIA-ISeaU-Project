package lx.iseau.feature.beach; // (패키지는 실제 경로에 맞게 수정하세요)

import java.time.LocalDateTime; // timestamp는 LocalDateTime이 더 적합합니다.
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBeachWeatherDTO {


    private int weatherNumber; 
    private double temperature;        // temperature (double precision)
    private int humidity;              // humidity (integer)
    private Integer rainProbability;       // rain_probability (integer)
    private double rain;               // rain (double precision) -> (JSON에서는 precipitation)
    private double windGusts;          // wind_gusts (double precision)
    private double windSpeed;          // wind_speed (double precision)
    private int windDirection;         // wind_direction (integer)
    private double uvIndex;            // uv_index (double precision)
    private double uvIndexClearSky;    // uv_index_clear_sky (double precision)
    private LocalDateTime forecastTime; // forecast_time (timestamp)
    private double latitude;
    private double longitude;
     private double lat;
     private double lon; 

}