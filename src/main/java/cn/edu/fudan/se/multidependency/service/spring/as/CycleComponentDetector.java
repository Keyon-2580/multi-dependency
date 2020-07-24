package cn.edu.fudan.se.multidependency.service.spring.as;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.relation.DependsOn;

public interface CycleComponentDetector {

	/**
	 * 包的循环依赖的检测
	 * @return
	 */
	Collection<Collection<DependsOn>> cyclePackages();
	
	/**
	 * 文件的循环依赖的检测
	 * @return
	 */
	Collection<Collection<DependsOn>> cycleFiles();
}
