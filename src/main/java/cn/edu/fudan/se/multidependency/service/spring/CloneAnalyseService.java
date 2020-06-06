package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;

public interface CloneAnalyseService {
	
	Collection<Collection<? extends CloneRelation>> groupFileCloneRelation(boolean removeDataClass);
	Collection<Collection<? extends CloneRelation>> groupFunctionCloneRelation(boolean removeFileClone);
	Collection<Collection<? extends Node>> groupFileCloneNode(boolean removeDataClass);
	Collection<Collection<? extends Node>> groupFunctionCloneNode(boolean removeFileClone);
	
//	Collection<>isCloneBetween
	
	/*Collection<Collection<Clone<Function, FunctionCloneFunction>>> queryFunctionCloneGroup();
	
	Collection<Collection<Clone<ProjectFile, FileCloneFile>>> queryFileCloneGroup();*/

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
	
	Collection<Clone<MicroService, FunctionCloneFunction>> findMicroServiceCloneFromFunctionClone(
			Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode);
	
	Collection<Clone<MicroService, FileCloneFile>> findMicroServiceCloneFromFileClone(
			Iterable<FileCloneFile> fileClones, boolean removeSameNode);
	
	Map<Integer, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFile(boolean removeDataClass);
	
	Map<Integer, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFunction(boolean removeFileLevelClone);
	
	Map<Project, CloneLineValue<Project>> projectCloneLineValues();
	
	Map<MicroService, CloneLineValue<MicroService>> msCloneLineValues(Iterable<MicroService> mss);
	
	CloneLineValue<MicroService> msCloneLineValuesGroup(MicroService ms, int group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
	Map<MicroService, CloneLineValue<MicroService>> msCloneLineValuesGroup(Iterable<MicroService> mss, int group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
//	Map<Integer, Map<MicroService, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(Collection<MicroService> mss);
//	
//	Map<Integer, Map<MicroService, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(Collection<MicroService> mss);
	
	Collection<MicroService> msSortByMsCloneLineCount(Collection<MicroService> mss, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
	Map<Integer, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(Collection<MicroService> mss, boolean removeDataClass);
	
	Map<Integer, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(Collection<MicroService> mss, boolean removeFileClone);
	
	boolean isCloneBetweenFiles(ProjectFile file1, ProjectFile file2);
}
