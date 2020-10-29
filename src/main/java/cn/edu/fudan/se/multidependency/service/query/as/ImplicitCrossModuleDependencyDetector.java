package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.as.data.LogicCouplingComponents;

public interface ImplicitCrossModuleDependencyDetector {
	
	void setMinCoChange(int minCoChange);
	
	int getMinCoChange();

	Collection<LogicCouplingComponents<ProjectFile>> cochangesInDifferentModule();
	
}
