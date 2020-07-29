package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.as.data.UnstableFile;

public interface UnstableDependencyDetector {

	Map<Project, List<UnstableFile>> unstableFiles();
	
}
