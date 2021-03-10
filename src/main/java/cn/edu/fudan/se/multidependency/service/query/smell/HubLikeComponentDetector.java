package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.data.FileHubLike;
import cn.edu.fudan.se.multidependency.service.query.smell.data.ModuleHubLike;
import cn.edu.fudan.se.multidependency.service.query.smell.data.PackageHubLike;

public interface HubLikeComponentDetector {
	
	Map<Long, List<FileHubLike>> fileHubLikes();

	Map<Long, List<PackageHubLike>> packageHubLikes();

	Map<Long, List<ModuleHubLike>> moduleHubLikes();
	
	int[] getProjectMinFileFanIO(Project project);
	
	void setProjectMinFileFanIO(Project project, int minFanIn, int minFanOut);

	int[] getProjectMinPackageFanIO(Project project);

	void setProjectMinPackageFanIO(Project project, int minFanIn, int minFanOut);
	
	int[] getProjectMinModuleFanIO(Project project);
	
	void setProjectMinModuleFanIO(Project project, int minFanIn, int minFanOut);

	int[] getProjectMinFileCoChangeFilesAndTimesThreshold(Project project);

	void setProjectMinFileCoChangeFilesAndTimesThreshold(Project project, int coChangeFilesThreshold, int coChangeTimesThreshold);

	int[] getProjectMinPackageCoChangeFilesAndTimesThreshold(Project project);

	void setProjectMinPackageCoChangeFilesAndTimesThreshold(Project project, int coChangeFilesThreshold, int coChangeTimesThreshold);

	int[] getProjectMinModuleCoChangeFilesAndTimesThreshold(Project project);

	void setProjectMinModuleCoChangeFilesAndTimesThreshold(Project project, int coChangeFilesThreshold, int coChangeTimesThreshold);

}
