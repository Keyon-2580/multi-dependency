package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.service.query.as.data.LogicCouplingFiles;

public interface ImplicitCrossModuleDependencyDetector {
	
	void setMinCoChange(int minCoChange);
	
	int getMinCoChange();

	Collection<LogicCouplingFiles> cochangesInDifferentModule();
	
}
