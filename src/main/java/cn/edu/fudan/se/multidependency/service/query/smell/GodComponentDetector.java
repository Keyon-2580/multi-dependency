package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.data.GodFile;
import cn.edu.fudan.se.multidependency.service.query.smell.data.GodPackage;

public interface GodComponentDetector {
	
	Map<Long, List<GodFile>> godFiles();
	
	Map<Long, List<GodPackage>> godPackages();
	
	int getProjectMinFileLoc(Project project);
	
	void setProjectMinFileLoc(Project project, int minFileLoc);
	
	int getProjectMinFileCountInPackage(Project project);
	
	void setProjectMinFileCountInPackage(Project project, int minFileCountInPackage);

}
