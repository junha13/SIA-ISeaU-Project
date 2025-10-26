package lx.iseau.feature.forecast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ForecastScheduler {
	
	@Autowired
	ForecastAPIBeachService BeachService;
	
	@Autowired
	ForecastAPIUVService UVService;
	
	@Autowired
	ForecastAPISeaService SeaService;

	@Scheduled(cron = "0 1 0-23 * * *", zone="Asia/Seoul")
	public void getForecastData() {
		BeachService.insertFirsttDB(BeachService.getFirstForecastData());
		UVService.insertSecondDB(UVService.getSecondForecastData());
		SeaService.insertSecondDB(SeaService.getSecondForecastData());		
	}
	
	@Scheduled(cron = "0 1 0-23 * * *", zone="Asia/Seoul")
	public void getRiptData() {
		
	}
	
}
