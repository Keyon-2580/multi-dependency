package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnutilizedAbstraction;

public interface UnutilizedAbstractionDetector {

	Map<Long, List<UnutilizedAbstraction<Type>>> unutilizedTypes();
	
	Map<Long, List<UnutilizedAbstraction<ProjectFile>>> unutilizedFiles();
	
}
