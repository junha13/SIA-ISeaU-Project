
package lx.iseau.feature.beach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeachService  { 

    @Autowired
    BeachDAO dao;
    @Transactional(readOnly = true)
    
    public List<BeachDTO> getBeachList(BeachListRequest request) {
        List<BeachVO> voList = dao.findBeaches(request);
        List<BeachDTO> dtoList = new ArrayList<>();      
        for (BeachVO vo : voList) {
            BeachDTO dto = new BeachDTO(
                vo.getBeachNumber(),
                vo.getBeachName(),
                vo.getBeachImage(),
                vo.getBeachInformation(),
                vo.getRating(),
                vo.isApprovedByMinistry(),
                vo.getAddress(),
                vo.getLatitude(),
                vo.getLongitude(), 
                vo.getMobile(),
                vo.getOpenDate(),
                vo.getCloseDate()
            );
            dtoList.add(dto);
        }
       
        Map<String, Object> map = new HashMap<String, Object>();
        
        
        map.put("result", dtoList);
        
      
        return dtoList;
    }

	
    
  
}