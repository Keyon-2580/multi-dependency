package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableFileInHistory;

public interface UnstableDependencyDetectorUsingHistory {

	Map<Long, List<UnstableFileInHistory>> unstableFiles();
	
	void setFanInThreshold(Project project, int minFanIn);
	
	void setCoChangeTimesThreshold(Project project, int cochangeTimesThreshold);
	
	void setCoChangeFilesThreshold(Project project, int cochangeFilesThreshold);
	
	int getFanInThreshold(Project project);
	
	int getCoChangeTimesThreshold(Project project);
	
	int getCoChangeFilesThreshold(Project project);
}
