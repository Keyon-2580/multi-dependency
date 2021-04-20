package cn.edu.fudan.se.multidependency.service.query.smell;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.service.query.smell.data.UnutilizedAbstraction;

public interface UnutilizedAbstractionDetector {
	/**
	 * 获取类的Unutilized Abstraction
	 */
	Map<Long, List<UnutilizedAbstraction<Type>>> getTypeUnutilizedAbstraction();

	/**
	 * 获取文件的Unutilized Abstraction
	 */
	Map<Long, List<UnutilizedAbstraction<ProjectFile>>> getFileUnutilizedAbstraction();

	/**
	 * 检测类的Unutilized Abstraction
	 */
	Map<Long, List<UnutilizedAbstraction<Type>>> detectTypeUnutilizedAbstraction();

	/**
	 * 检测文件的Unutilized Abstraction
	 */
	Map<Long, List<UnutilizedAbstraction<ProjectFile>>> detectFileUnutilizedAbstraction();
	
}
