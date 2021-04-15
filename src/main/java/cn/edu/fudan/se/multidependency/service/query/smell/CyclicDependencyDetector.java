package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.service.query.smell.data.Cycle;
import com.alibaba.fastjson.JSONObject;

public interface CyclicDependencyDetector {

	/**
	 * 获取类的循环依赖
	 */
	Map<Long, Map<Integer, Cycle<Type>>> getTypeCyclicDependency();

	/**
	 * 获取文件的循环依赖
	 */
	Map<Long, Map<Integer, Cycle<ProjectFile>>> getFileCyclicDependency();

	/**
	 * 获取包的循环依赖
	 */
	Map<Long, Map<Integer, Cycle<Package>>> getPackageCyclicDependency();

	/**
	 * 获取模块的循环依赖
	 */
	Map<Long, Map<Integer, Cycle<Module>>> getModuleCyclicDependency();

	/**
	 * 检测类的循环依赖
	 */
	Map<Long, Map<Integer, Cycle<Type>>> detectTypeCyclicDependency();

	/**
	 * 检测文件的循环依赖
	 */
	Map<Long, Map<Integer, Cycle<ProjectFile>>> detectFileCyclicDependency();

	/**
	 * 检测包的循环依赖
	 */
	Map<Long, Map<Integer, Cycle<Package>>> detectPackageCyclicDependency();

	/**
	 * 检测模块的循环依赖
	 */
	Map<Long, Map<Integer, Cycle<Module>>> detectModuleCyclicDependency();

	/**
	 * 根据file的Id生成文件所在的循环依赖的json格式信息
	 */
	JSONObject getCyclicDependencyJson(Long fileId);

	void exportCycleDependency();
}
