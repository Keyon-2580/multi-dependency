package cn.edu.fudan.se.multidependency.service.spring.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;

public interface ImplicitCrossModuleDependencyDetector {

	Collection<CoChange> cochangesInDifferentModule(int minCochange);
	
}
