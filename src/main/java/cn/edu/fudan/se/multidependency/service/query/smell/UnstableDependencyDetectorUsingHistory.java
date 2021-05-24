package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableDependencyByHistory;

public interface UnstableDependencyDetectorUsingHistory {

	Map<Long, List<UnstableDependencyByHistory>> queryUnstableDependency();

	Map<Long, List<UnstableDependencyByHistory>> detectUnstableDependency();

	void setFanOutThreshold(Long projectId, Integer minFanOut);

	void setCoChangeTimesThreshold(Long projectId, Integer cochangeTimesThreshold);

	void setCoChangeFilesThreshold(Long projectId, Integer cochangeFilesThreshold);

	void setProjectMinRatio(Long projectId, Double minRatio);

	Integer getFanOutThreshold(Long projectId);

	Integer getCoChangeTimesThreshold(Long projectId);

	Integer getCoChangeFilesThreshold(Long projectId);

	Double getProjectMinRatio(Long projectId);
}
