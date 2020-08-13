package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.data.GodFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.GodPackage;

public interface GodComponentDetector {
	
	Map<Long, List<GodFile>> godFiles();
	
	Map<Long, List<GodPackage>> godPackages();
	
	int getProjectMinFileLoc(Project project);
	
	void setProjectMinFileLoc(Project project, int minFileLoc);
	
	int getProjectMinFileCountInPackage(Project project);
	
	void setProjectMinFileCountInPackage(Project project, int minFileCountInPackage);

}
