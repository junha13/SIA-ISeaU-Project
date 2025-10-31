package lx.iseau.feature.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class RequestUserLocationDTO {

	private int userNumber;
	private int groupNumber;
	private double latitude;
	private double longitude;
}
