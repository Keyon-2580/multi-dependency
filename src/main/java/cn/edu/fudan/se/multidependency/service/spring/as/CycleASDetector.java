package cn.edu.fudan.se.multidependency.service.spring.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.relation.DependOn;

public interface CycleASDetector {

	/**
	 * 包的循环依赖的检测
	 * @return
	 */
	Collection<Collection<DependOn>> findCyclePackages();
}