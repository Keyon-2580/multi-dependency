package cn.edu.fudan.se.multidependency.service.query.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.service.query.as.data.LogicCoupling;

public interface ImplicitCrossModuleDependencyDetector {

	Collection<LogicCoupling> cochangesInDifferentModule(int minCochange);
	
}
