package lx.iseau.feature.beach;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BeachDAO {
	List<BeachVO> findBeaches(BeachListRequest request);

}