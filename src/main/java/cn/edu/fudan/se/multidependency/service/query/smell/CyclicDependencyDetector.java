package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;

public interface CyclicDependencyDetector {

	/**
	 * 包的循环依赖的检测
	 * @return
	 */
	Map<Long, Map<Integer, Cycle<Package>>> cyclePackages();

	/**
	 * 模块的循环依赖的检测
	 * @return
	 */
	Map<Long, Map<Integer, Cycle<Module>>> cycleModules();
	
	/**
	 * 文件的循环依赖的检测
	 * @return
	 */
	Map<Long, Map<Integer, Cycle<ProjectFile>>> cycleFiles();

	/**
	 * 类的循环依赖的检测
	 * @return
	 */
	Map<Long, Map<Integer, Cycle<Type>>> cycleTypes();

	void exportCycleDependency();
}
