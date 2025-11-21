package lx.iseau.feature.cctv;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/cctv")
@RestController
@RequiredArgsConstructor
public class CctvController {

   private final CctvService service;

   // =========================
   // 로그 넣기 ( 파이썬에서 호출 )
   // Endpoint: POST /api/cctv/addLog
   // =========================
   @PostMapping("/addLog")
   public ResponseEntity<?> addLog(@RequestBody CctvDangerLogDto logDto) {
	   log.info("📥 /api/cctv/logList 요청 수신: {}");
	   Map<String, Object> result = service.insertDangerLog(logDto);
	   return ResponseEntity
			   .ok()
			   .body(Map.of("result", result));
   }
   
   // =========================
   // 로그리스트 가져오기
   // Endpoint: POST /api/cctv/logList 
   // =========================
   @PostMapping("/logList")
   public ResponseEntity<?> getLogList(@RequestBody CctvDangerLogDto logDto) {
       log.info("📥 /api/cctv/logList 요청 수신: {}");
       Map<String, Object> result = service.selectDangerLogList(logDto);
       return ResponseEntity
               .ok()
               .body(Map.of("result", result));
   }
   
}
