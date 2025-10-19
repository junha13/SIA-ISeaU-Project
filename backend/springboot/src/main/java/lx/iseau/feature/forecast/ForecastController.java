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
	ForecastAPIService service;

	/*
	 * 설명
	 */
	@RequestMapping("/a")
	public String a() {
		service.insertDB(service.getForecastData());
		return "";
	}
}
