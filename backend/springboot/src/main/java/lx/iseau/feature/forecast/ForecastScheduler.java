package lx.iseau.feature.forecast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ForecastScheduler {
	
	@Autowired
	ForecastAPIService service;

	@Scheduled(cron = "0 1 0-23 * * *", zone="Asia/Seoul")
	public void getForecastData() {
		service.insertDB(service.getForecastData());
		
	}
	
	@Scheduled(cron = "0 1 0-23 * * *", zone="Asia/Seoul")
	public void getRiptData() {
		
	}
	
}
