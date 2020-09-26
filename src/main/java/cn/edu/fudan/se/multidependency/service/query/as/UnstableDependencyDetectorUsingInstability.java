package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableComponentByInstability;

public interface UnstableDependencyDetectorUsingInstability {

	Map<Long, List<UnstableComponentByInstability<Package>>> unstablePackages();
	
	Map<Long, List<UnstableComponentByInstability<Module>>> unstableModules();
	
	Map<Long, List<UnstableComponentByInstability<ProjectFile>>> unstableFiles();
	
	void setRatio(Project project, double threshold);
	
	void setFileFanOutThreshold(Project project, int threshold);
	
	void setModuleFanOutThreshold(Project project, int threshold);
	
	int getFileFanOutThreshold(Project project);
	
	int getModuleFanOutThreshold(Project project);
	
	double getRatioThreshold(Project project);
}
