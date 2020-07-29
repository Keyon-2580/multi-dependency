package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.Cycle;

public interface CyclicDependencyDetector {

	/**
	 * 包的循环依赖的检测
	 * @return
	 */
	Map<Project, List<Cycle<Package>>> cyclePackages(boolean withRelation);
	
	/**
	 * 文件的循环依赖的检测
	 * @return
	 */
	Map<Project, List<Cycle<ProjectFile>>> cycleFiles(boolean withRelation);
}
