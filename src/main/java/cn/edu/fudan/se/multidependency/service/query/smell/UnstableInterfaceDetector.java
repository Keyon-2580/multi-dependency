package cn.edu.fudan.se.multidependency.service.query.smell;

import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableInterface;

import java.util.List;
import java.util.Map;

public interface UnstableInterfaceDetector {

	Map<Long, List<UnstableInterface>> queryUnstableInterface();

	Map<Long, List<UnstableInterface>> detectUnstableInterface();

	void setFanInThreshold(Long projectId, Integer minFanIn);
	
	void setCoChangeTimesThreshold(Long projectId, Integer cochangeTimesThreshold);
	
	void setCoChangeFilesThreshold(Long projectId, Integer cochangeFilesThreshold);

	void setProjectMinRatio(Long projectId, Double minRatio);

	Integer getFanInThreshold(Long projectId);

	Integer getCoChangeTimesThreshold(Long projectId);

	Integer getCoChangeFilesThreshold(Long projectId);

	Double getProjectMinRatio(Long projectId);
}
