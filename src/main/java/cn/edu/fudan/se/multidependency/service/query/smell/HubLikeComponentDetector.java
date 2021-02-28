package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.smell.data.HubLikeModule;

public interface HubLikeComponentDetector {
	
	Map<Long, List<HubLikeFile>> hubLikeFiles();
	
	Map<Long, List<HubLikeModule>> hubLikeModules();
	
	int[] getProjectMinFileFanIO(Project project);
	
	void setProjectMinFileFanIO(Project project, int minFanIn, int minFanOut);
	
	int[] getProjectMinModuleFanIO(Project project);
	
	void setProjectMinModuleFanIO(Project project, int minFanIn, int minFanOut);

	int[] getProjectMinFileCoChangeFilesAndTimesThreshold(Project project);

	void setProjectMinFileCoChangeFilesAndTimesThreshold(Project project, int coChangeFilesThreshold, int coChangeTimesThreshold);

	int[] getProjectMinModuleCoChangeFilesAndTimesThreshold(Project project);

	void setProjectMinModuleCoChangeFilesAndTimesThreshold(Project project, int coChangeFilesThreshold, int coChangeTimesThreshold);

}
