package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;

public interface CloneAnalyseService {
	
	Collection<Collection<? extends CloneRelation>> groupFileCloneRelation();
	Collection<Collection<? extends CloneRelation>> groupFunctionCloneRelation();
	Collection<Collection<? extends Node>> groupFileCloneNode();
	Collection<Collection<? extends Node>> groupFunctionCloneNode();
	
	Collection<Collection<Clone<Function, FunctionCloneFunction>>> queryFunctionCloneGroup();
	
	Collection<Collection<Clone<ProjectFile, FileCloneFile>>> queryFileCloneGroup();

	public Iterable<FunctionCloneFunction> findAllFunctionCloneFunctions();
	
	public Iterable<FileCloneFile> findAllFileCloneFiles();
	
	public Iterable<FunctionCloneFunction> findProjectContainFunctionCloneFunctions(Project project);
	
	public Iterable<FileCloneFile> queryProjectContainFileCloneFiles(Project project);
	
	/**
	 * 根据函数间的克隆找出项目间的克隆
	 * @param functionClones
	 * @return
	 */
	public Collection<Clone<Project, FunctionCloneFunction>> findProjectCloneFromFunctionClone(Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode);
	
	/**
	 * 根据文件间的克隆找出项目间的克隆
	 * @param fileClones
	 * @param removeSameNode
	 * @return
	 */
	public Collection<Clone<Project, FileCloneFile>> queryProjectCloneFromFileClone(Iterable<FileCloneFile> fileClones, boolean removeSameNode);
	
}
