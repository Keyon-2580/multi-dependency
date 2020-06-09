package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;

public interface BasicCloneQueryService {
	
	/**
	 * 所有方法克隆关系
	 * @return
	 */
	Iterable<FunctionCloneFunction> findAllFunctionCloneFunctions();
	
	/**
	 * 所有文件克隆关系
	 * @return
	 */
	Iterable<FileCloneFile> findAllFileCloneFiles();
	
	/**
	 * 查询文件级或方法级的所有克隆组
	 * @param level
	 * @return
	 */
	Collection<CloneGroup> queryGroups(CloneLevel level);
	
	/**
	 * 查询一个克隆组包含的所有方法克隆关系
	 * @param group
	 * @return
	 */
	Collection<FunctionCloneFunction> queryGroupContainFunctionClones(CloneGroup group);
	
	/**
	 * 查询一个克隆组包含的所有文件克隆关系
	 * @param group
	 * @return
	 */
	Collection<FileCloneFile> queryGroupContainFileClones(CloneGroup group);
	
	/**
	 * 查询一个项目内的方法克隆关系
	 * @param project
	 * @return
	 */
	Iterable<FunctionCloneFunction> queryProjectContainFunctionCloneFunctions(Project project);
	
	/**
	 * 查询一个项目内的文件克隆关系
	 * @param project
	 * @return
	 */
	Iterable<FileCloneFile> queryProjectContainFileCloneFiles(Project project);
}
