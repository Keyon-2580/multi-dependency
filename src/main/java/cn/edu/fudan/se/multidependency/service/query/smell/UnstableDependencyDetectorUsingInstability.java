package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnstableComponentByInstability;

public interface UnstableDependencyDetectorUsingInstability {

	Map<Long, List<UnstableComponentByInstability<ProjectFile>>> queryFileUnstableDependency();

	Map<Long, List<UnstableComponentByInstability<Package>>> queryPackageUnstableDependency();
	
	Map<Long, List<UnstableComponentByInstability<Module>>> queryModuleUnstableDependency();

	Map<Long, List<UnstableComponentByInstability<ProjectFile>>> detectFileUnstableDependency();

	Map<Long, List<UnstableComponentByInstability<Package>>> detectPackageUnstableDependency();

	Map<Long, List<UnstableComponentByInstability<Module>>> detectModuleUnstableDependency();
	
	void setProjectMinFileFanOut(Long projectId, Integer minFileFanOut);
	
	void setProjectMinPackageFanOut(Long projectId, Integer minPackageFanOut);

	void setProjectMinModuleFanOut(Long projectId, Integer minModuleFanOut);

	void setProjectMinRatio(Long projectId, Double minRatio);
	
	Integer getProjectMinFileFanOut(Long projectId);

	Integer getProjectMinPackageFanOut(Long projectId);

	Integer getProjectMinModuleFanOut(Long projectId);
	
	Double getProjectMinRatio(Long projectId);
}
