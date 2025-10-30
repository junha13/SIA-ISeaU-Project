package lx.iseau.feature.location;

import org.apache.ibatis.annotations.Mapper;

import lx.iseau.feature.group.RequestGroupDTO;

@Mapper
public interface LocationDAO {

	ResponseUserLocationDTO selectBoundaryCheck(RequestUserLocationDTO dto);
	
	ResponseUserLocationDTO selectTestCheck(RequestUserLocationDTO dto);

}
