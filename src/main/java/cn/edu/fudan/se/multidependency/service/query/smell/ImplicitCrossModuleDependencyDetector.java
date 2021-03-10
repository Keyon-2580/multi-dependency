package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.smell.data.LogicCouplingComponents;

public interface ImplicitCrossModuleDependencyDetector {
	
	void setFileMinCoChange(int minCoChange);
	
	int getFileMinCoChange();

	void setPackageMinCoChange(int minCoChange);

	int getPackageMinCoChange();

	Collection<LogicCouplingComponents<ProjectFile>> cochangesInDifferentFile();

	Collection<LogicCouplingComponents<Package>> cochangesInDifferentPackage();

}
