package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;

public interface CyclicDependencyDetector {

	/**
	 * 类的循环依赖的检测
	 * @return
	 */
	Map<Long, Map<Integer, Cycle<Type>>> typeCycles();

	/**
	 * 文件的循环依赖的检测
	 * @return
	 */
	Map<Long, Map<Integer, Cycle<ProjectFile>>> fileCycles();

	/**
	 * 包的循环依赖的检测
	 * @return
	 */
	Map<Long, Map<Integer, Cycle<Package>>> packageCycles();

	/**
	 * 模块的循环依赖的检测
	 * @return
	 */
	Map<Long, Map<Integer, Cycle<Module>>> moduleCycles();

	void exportCycleDependency();
}
