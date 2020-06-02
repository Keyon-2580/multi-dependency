package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;

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
	
	Collection<Clone<MicroService, FunctionCloneFunction>> findMicroServiceCloneFromFunctionClone(
			Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode);
	
	Collection<Clone<MicroService, FileCloneFile>> findMicroServiceCloneFromFileClone(
			Iterable<FileCloneFile> fileClones, boolean removeSameNode);
	
	JSONObject clonesToCytoscape(Collection<? extends CloneRelation> groupRelations);
	
	Map<Integer, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFile();
	
	Map<Integer, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFunction();
	
	Map<Project, CloneLineValue<Project>> projectCloneLineValues();
	
	Map<MicroService, CloneLineValue<MicroService>> msCloneLineValues(Iterable<MicroService> mss);
	
	CloneLineValue<MicroService> msCloneLineValuesGroup(MicroService ms, int group, CloneLevel level);
	
	Map<MicroService, CloneLineValue<MicroService>> msCloneLineValuesGroup(Iterable<MicroService> mss, int group, CloneLevel level);
	
//	Map<Integer, Map<MicroService, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(Collection<MicroService> mss);
//	
//	Map<Integer, Map<MicroService, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(Collection<MicroService> mss);
	
	Collection<MicroService> msSortByMsCloneLineCount(Collection<MicroService> mss, CloneLevel level);
	
	Map<Integer, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(Collection<MicroService> mss);
	
	Map<Integer, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(Collection<MicroService> mss);
}
