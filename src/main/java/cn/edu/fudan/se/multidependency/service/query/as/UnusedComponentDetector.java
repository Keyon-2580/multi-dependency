package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;

public interface UnusedComponentDetector {

	Map<Project, List<Package>> unusedPackage();
	
}
