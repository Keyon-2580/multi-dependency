package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;

public interface UnusedComponentDetector {

	Map<Long, List<Package>> unusedPackage();
	
}
