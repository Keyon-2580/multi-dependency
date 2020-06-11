package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;
import cn.edu.fudan.se.multidependency.service.spring.data.FileCloneGroup;
import cn.edu.fudan.se.multidependency.service.spring.data.FunctionCloneGroup;

public interface CloneAnalyseService {
	
	Collection<Project> fileCloneGroupContainProjects(FileCloneGroup group);
	
	Collection<Project> functionCloneGroupContainProjects(FunctionCloneGroup group);
	
	Collection<MicroService> fileCloneGroupContainMSs(FileCloneGroup group);
	
	Collection<MicroService> functionCloneGroupContainMSs(FunctionCloneGroup group);
	
	Collection<FileCloneGroup> groupFileClones(boolean removeDataClass);
	
	Collection<FunctionCloneGroup> groupFunctionClones(boolean removeFileClone);
	
	Collection<FileCloneGroup> groupFileClonesContainProjects(Collection<FileCloneGroup> groups, Collection<Project> projects);
	
	Collection<FunctionCloneGroup> groupFunctionClonesContainProjects(Collection<FunctionCloneGroup> groups, Collection<Project> projects);
	
	Collection<FileCloneGroup> groupFileClonesContainMSs(Collection<FileCloneGroup> groups, Collection<MicroService> mss);
	
	Collection<FunctionCloneGroup> groupFunctionClonesContainMSs(Collection<FunctionCloneGroup> groups, Collection<MicroService> mss);
	
//	Map<Project, CloneLineValue<Project>> projectCloneLineValues();
	
//	Map<MicroService, CloneLineValue<MicroService>> msCloneLineValues(Iterable<MicroService> mss);
	
	CloneLineValue<MicroService> msCloneLineValuesGroup(MicroService ms, CloneGroup group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
	Collection<MicroService> msSortByMsCloneLineCount(Collection<MicroService> mss, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
	Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFile(boolean removeDataClass);
	
	Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFunction(boolean removeFileLevelClone);
	
	Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(Collection<MicroService> mss, boolean removeDataClass);
	
	Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(Collection<MicroService> mss, boolean removeFileClone);
	
	/**
	 * 根据俩文件是否在同一克隆组判断是否有克隆关系
	 * @param file1
	 * @param file2
	 * @return
	 */
	boolean isCloneBetweenFiles(ProjectFile file1, ProjectFile file2);
	
	// MDAllController
	
	public Map<MicroService, CloneLineValue<MicroService>> msCloneLineValuesGroup(Iterable<MicroService> mss, CloneGroup group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
	/**
	 * 根据函数间的克隆找出微服务间的克隆
	 * @param functionClones
	 * @return
	 */
	public Collection<Clone<MicroService, FunctionCloneFunction>> findMicroServiceCloneFromFunctionClone(Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode);
	
	/**
	 * 根据文件间的克隆找出微服务间的克隆
	 * @param fileClones
	 * @param removeSameNode
	 * @return
	 */
	public Collection<Clone<MicroService, FileCloneFile>> findMicroServiceCloneFromFileClone(Iterable<FileCloneFile> fileClones, boolean removeSameNode);
	
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
