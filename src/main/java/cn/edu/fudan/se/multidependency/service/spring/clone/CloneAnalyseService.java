package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;

public interface CloneAnalyseService {
	/**
	 * 根据克隆关系类型找出克隆组
	 * 并根据条件移除
	 * @param cloneRelationType
	 * @param predicates
	 * @return
	 */
	Collection<CloneGroup> group(CloneRelationType cloneRelationType, Collection<Predicate<CloneGroup>> predicates);
	
	CloneGroup addNodeAndRelationToCloneGroup(CloneGroup cloneGroup);
	
	/**
	 * 给一个克隆组，找出其中包含的项目
	 * @param group
	 * @return
	 */
	Collection<Project> cloneGroupContainProjects(CloneGroup group);
	
	/**
	 * 给多个克隆组，找出其中包含的项目
	 * @param group
	 * @return
	 */
	Collection<Project> cloneGroupContainProjects(Collection<CloneGroup> groups);

	/**
	 * 给一个克隆组，找出其中包含的微服务项目
	 * @param group
	 * @return
	 */
	Collection<MicroService> cloneGroupContainMicroServices(CloneGroup group);
	
	/**
	 * 给多个克隆组，找出其中包含的微服务项目
	 * @param group
	 * @return
	 */
	Collection<MicroService> cloneGroupContainMicroServices(Collection<CloneGroup> groups);
	
	/**
	 * 从给定的克隆组中找出包含指定项目的克隆组
	 * @param groups
	 * @param projects
	 * @return
	 */
	Collection<CloneGroup> findGroupsContainProjects(Collection<CloneGroup> groups, Collection<Project> projects);
	
	/**
	 * 从给定的克隆组中找出包含指定微服务项目的克隆组
	 * @param groups
	 * @param mss
	 * @return
	 */
	Collection<CloneGroup> findGroupsContainMicroServices(Collection<CloneGroup> groups, Collection<MicroService> mss);
	
//	CloneLineValue<MicroService> msCloneLineValuesGroup(MicroService ms, CloneGroup group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
//	
//	Collection<MicroService> msSortByMsCloneLineCount(CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
	
	Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectCloneLineValues(Collection<CloneGroup> groups);
	
	/*Map<String, Map<Long, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFile(boolean removeDataClass, Language language);
	
	Map<String, Map<Long, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFunction(boolean removeFileLevelClone, Language language);
	
	Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(boolean removeDataClass);
	
	Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(boolean removeFileClone);
	
	Map<MicroService, CloneLineValue<MicroService>> msCloneLineValuesGroup(Iterable<MicroService> mss, CloneGroup group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass);
*/
	/**
	 * 根据俩文件是否在同一克隆组判断是否有克隆关系
	 * @param file1
	 * @param file2
	 * @return
	 */
	boolean isCloneBetweenFiles(ProjectFile file1, ProjectFile file2);
	
//	String exportCloneMicroService(Map<String, Map<Long, CloneLineValue<MicroService>>> data, Collection<MicroService> microservices, CloneRelationType cloneRelationType);
//	
//	String exportCloneProject(Map<String, Map<Long, CloneLineValue<Project>>> data, Collection<Project> projects, CloneRelationType cloneRelationType);
}
