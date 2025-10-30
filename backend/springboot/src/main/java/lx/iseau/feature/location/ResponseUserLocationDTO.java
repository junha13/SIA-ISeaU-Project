package lx.iseau.feature.location;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lx.iseau.feature.group.GroupInviteRequestDTO;
import lx.iseau.feature.group.GroupsService;
import lx.iseau.feature.group.ResponseGroupDTO;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class ResponseUserLocationDTO {

	private int groupNumber;
	private boolean inside; // 들어갔는지 여부
	private Double distance; // 경계선까지의 거리
	private int interval;
}
