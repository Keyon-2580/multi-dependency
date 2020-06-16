package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Language;
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
	/**
	 * 获取文件级克隆组的克隆节点和关系
	 * @param removeDataClass
	 * @return
	 */
	Collection<FileCloneGroup> groupFileClones(boolean removeDataClass, Language language);
	
	/**
	 * 获取方法级克隆组的克隆节点和关系
	 * @param removeFileClone
	 * @return
	 */
	Collection<FunctionCloneGroup> groupFunctionClones(boolean removeFileClone, Language language);
	
	Collection<Project> fileCloneGroupContainProjects(FileCloneGroup group, Language language);
	
	Collection<Project> functionCloneGroupContainProjects(FunctionCloneGroup group, Language language);
	
	Collection<MicroService> fileCloneGroupContainMSs(FileCloneGroup group);
	
	Collection<MicroService> functionCloneGroupContainMSs(FunctionCloneGroup group);
	
	Collection<FileCloneGroup> groupFileClonesContainProjects(Collection<FileCloneGroup> groups, Collection<Project> projects);
	
	Collection<FunctionCloneGroup> groupFunctionClonesContainProjects(Collection<FunctionCloneGroup> groups, Collection<Project> projects);
	
	Collection<FileCloneGroup> groupFileClonesContainMSs(Collection<FileCloneGroup> groups, Collection<MicroService> mss);
	
	Collection<FunctionCloneGroup> groupFunctionClonesContainMSs(Collection<FunctionCloneGroup> groups, Collection<MicroService> mss);
	
	CloneLineValue<MicroService> msCloneLineValuesGroup(MicroService ms, CloneGroup group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
	Collection<MicroService> msSortByMsCloneLineCount(CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
	Map<String, Map<Long, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFile(boolean removeDataClass, Language language);
	
	Map<String, Map<Long, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFunction(boolean removeFileLevelClone, Language language);
	
	Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(boolean removeDataClass);
	
	Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(boolean removeFileClone);
	
	/**
	 * 根据俩文件是否在同一克隆组判断是否有克隆关系
	 * @param file1
	 * @param file2
	 * @return
	 */
	boolean isCloneBetweenFiles(ProjectFile file1, ProjectFile file2);
	
	// MDAllController
	
	Map<MicroService, CloneLineValue<MicroService>> msCloneLineValuesGroup(Iterable<MicroService> mss, CloneGroup group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
	/**
	 * 根据函数间的克隆找出微服务间的克隆
	 * @param functionClones
	 * @return
	 */
	Collection<Clone<MicroService, FunctionCloneFunction>> findMicroServiceCloneFromFunctionClone(Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode);
	
	/**
	 * 根据文件间的克隆找出微服务间的克隆
	 * @param fileClones
	 * @param removeSameNode
	 * @return
	 */
	Collection<Clone<MicroService, FileCloneFile>> findMicroServiceCloneFromFileClone(Iterable<FileCloneFile> fileClones, boolean removeSameNode);
	
	/**
	 * 根据函数间的克隆找出项目间的克隆
	 * @param functionClones
	 * @return
	 */
	Collection<Clone<Project, FunctionCloneFunction>> findProjectCloneFromFunctionClone(Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode);
	
	/**
	 * 根据文件间的克隆找出项目间的克隆
	 * @param fileClones
	 * @param removeSameNode
	 * @return
	 */
	Collection<Clone<Project, FileCloneFile>> queryProjectCloneFromFileClone(Iterable<FileCloneFile> fileClones, boolean removeSameNode);

	String exportCloneMicroService(Map<String, Map<Long, CloneLineValue<MicroService>>> data, Collection<MicroService> microservices, CloneLevel level);

	String exportCloneProject(Map<String, Map<Long, CloneLineValue<Project>>> data, Collection<Project> projects, CloneLevel level);
}
