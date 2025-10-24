package lx.iseau.feature.forecast;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/forecast")
@RestController
public class ForecastController {
	
	@Autowired
	ForecastAPIBeachService BeachService;
	
	@Autowired
	ForecastAPIUVService UVService;
	
	@Autowired
	ForecastAPISeaService SeaService;

	/*
	 * 설명
	 */
	@RequestMapping("/a")
	public String a() {
		BeachService.insertFirsttDB(BeachService.getFirstForecastData());
		UVService.insertSecondDB(UVService.getSecondForecastData());
		SeaService.insertSecondDB(SeaService.getSecondForecastData());	
		return "";
	}
}
