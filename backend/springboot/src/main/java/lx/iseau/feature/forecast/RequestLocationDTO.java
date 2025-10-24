package lx.iseau.feature.forecast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestLocationDTO {

	private int beachNumber;
	private double lat;
	private double lon;
}
