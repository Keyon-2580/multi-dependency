package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstablePackage;

public interface UnstableDependencyDetector {
	
	void setFanInThreshold(Project project, int minFanIn);
	
	void setCoChangeTimesThreshold(Project project, int cochangeTimesThreshold);
	
	void setCoChangeFilesThreshold(Project project, int cochangeFilesThreshold);
	
	int getFanInThreshold(Project project);
	
	int getCoChangeTimesThreshold(Project project);
	
	int getCoChangeFilesThreshold(Project project);

	Map<Long, List<UnstableFile>> unstableFiles();
	
	Map<Long, List<UnstablePackage>> unstablePackages();
	
}
