package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikeFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.HubLikePackage;

public interface HubLikeComponentDetector {
	
	Map<Long, List<HubLikeFile>> hubLikeFiles();
	
	Map<Long, List<HubLikePackage>> hubLikePackages();
	
	List<HubLikePackage> hubLikePackages(Project project);
	
	List<HubLikeFile> hubLikeFiles(Project project);
	
	double defaultFileMinFanIn(Project project);
	
	double defaultFileMinFanOut(Project project);
	
	double defaultPackageMinFanIn(Project project);
	
	double defaultPackageMinFanOut(Project project);
	
	int[] getProjectMinFileFanIO(Project project);
	
	void setProjectMinFileFanIO(Project project, int minFanIn, int minFanOut);
	
	int[] getProjectMinPackageFanIO(Project project);
	
	void setProjectMinPackageFanIO(Project project, int minFanIn, int minFanOut);
	
	void resetProjectMinFileFanIO();
	
	void resetProjectMinPackageFanIO();
	
	void resetProjectMinFileFanIO(int minFanIn, int minFanOut);
	
	void resetProjectMinPackageFanIO(int minFanIn, int minFanOut);
}
