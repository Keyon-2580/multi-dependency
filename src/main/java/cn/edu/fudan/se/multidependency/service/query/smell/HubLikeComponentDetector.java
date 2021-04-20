package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.data.FileHubLike;
import cn.edu.fudan.se.multidependency.service.query.smell.data.ModuleHubLike;
import cn.edu.fudan.se.multidependency.service.query.smell.data.PackageHubLike;

public interface HubLikeComponentDetector {
	
	Map<Long, List<FileHubLike>> queryFileHubLike();

	Map<Long, List<PackageHubLike>> queryPackageHubLike();

	Map<Long, List<ModuleHubLike>> queryModuleHubLike();

	Map<Long, List<FileHubLike>> detectFileHubLike();

	Map<Long, List<PackageHubLike>> detectPackageHubLike();

	Map<Long, List<ModuleHubLike>> detectModuleHubLike();
	
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
