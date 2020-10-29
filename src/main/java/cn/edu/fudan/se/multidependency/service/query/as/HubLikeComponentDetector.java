package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeModule;

public interface HubLikeComponentDetector {
	
	Map<Long, List<HubLikeFile>> hubLikeFiles();
	
	Map<Long, List<HubLikeModule>> hubLikeModules();
	
	int[] getProjectMinFileFanIO(Project project);
	
	void setProjectMinFileFanIO(Project project, int minFanIn, int minFanOut);
	
	int[] getProjectMinModuleFanIO(Project project);
	
	void setProjectMinModuleFanIO(Project project, int minFanIn, int minFanOut);
	
}
