package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FileCloneFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FunctionCloneFunctionRepository;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneValueCalculatorForMicroService;
import cn.edu.fudan.se.multidependency.service.spring.data.FileCloneGroup;
import cn.edu.fudan.se.multidependency.service.spring.data.FunctionCloneGroup;

@Service
public class CloneAnalyseServiceImpl implements CloneAnalyseService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CloneAnalyseServiceImpl.class);
    
    @Autowired
    FunctionCloneFunctionRepository functionCloneFunctionRepository;
    
    @Autowired
    FileCloneFileRepository fileCloneFileRepository;
    
    @Autowired
    ContainRelationService containRelationService;
    
    @Autowired
    StaticAnalyseService staticAnalyseService;
    
    @Autowired
    CacheService cacheService;
    
    @Autowired
    MicroserviceService msService;
	
    @Autowired
    BasicCloneQueryService basicCloneQueryService;

	Map<Boolean, Collection<FileCloneGroup>> groupFileCloneRelationCache = new ConcurrentHashMap<>();
    @Override
	public Collection<FileCloneGroup> groupFileClones(boolean removeDataClass) {
		if(groupFileCloneRelationCache.get(removeDataClass) != null) {
			return groupFileCloneRelationCache.get(removeDataClass);
		}
		Collection<CloneGroup> groups = basicCloneQueryService.queryGroups(CloneLevel.file);
		List<FileCloneGroup> result = new ArrayList<>();
		for(CloneGroup group : groups) {
			FileCloneGroup fileGroup = new FileCloneGroup(group);
			Collection<FileCloneFile> relations = basicCloneQueryService.queryGroupContainFileClones(group);
			for(FileCloneFile relation : relations) {
				ProjectFile file1 = relation.getFile1();
				ProjectFile file2 = relation.getFile2();
				if(removeDataClass) {
					if(staticAnalyseService.isDataFile(file1) || staticAnalyseService.isDataFile(file2)) {
						continue;
					}
				}
				fileGroup.addRelation(relation);
				fileGroup.addFile(file1);
				fileGroup.addFile(file2);
			}
			result.add(fileGroup);
		}
		result.sort((group1, group2) -> {
			return group2.getFiles().size() - group1.getFiles().size();
		});
		groupFileCloneRelationCache.put(removeDataClass, result);
		return result;
	}
	
	Map<Boolean, Collection<FunctionCloneGroup>> groupFunctionCloneRelationCache = new ConcurrentHashMap<>();
    @Override
	public Collection<FunctionCloneGroup> groupFunctionClones(boolean removeFileClone) {
		if(groupFunctionCloneRelationCache.get(removeFileClone) != null) {
			return groupFunctionCloneRelationCache.get(removeFileClone);
		}
		Collection<CloneGroup> groups = basicCloneQueryService.queryGroups(CloneLevel.function);
		List<FunctionCloneGroup> result = new ArrayList<>();
		for(CloneGroup group : groups) {
			FunctionCloneGroup functionGroup = new FunctionCloneGroup(group);
			Collection<FunctionCloneFunction> relations = basicCloneQueryService.queryGroupContainFunctionClones(group);
			for(FunctionCloneFunction relation : relations) {
				Function function1 = relation.getFunction1();
				Function function2 = relation.getFunction2();
				if(removeFileClone) {
					ProjectFile file1 = containRelationService.findFunctionBelongToFile(function1);
					ProjectFile file2 = containRelationService.findFunctionBelongToFile(function2);
					if(isCloneBetweenFiles(file1, file2)) {
						continue;
					}
				}
				functionGroup.addRelation(relation);
				functionGroup.addFunction(function1);
				functionGroup.addFunction(function2);
			}
			result.add(functionGroup);
		}
		result.sort((group1, group2) -> {
			return group2.getFunctions().size() - group1.getFunctions().size();
		});
		groupFunctionCloneRelationCache.put(removeFileClone, result);
		return result;
	}
    
    Map<Boolean, Map<CloneGroup, Map<Project, CloneLineValue<Project>>>> projectCloneValuesCalculateByFileCache = new ConcurrentHashMap<>();
    @Override
    public Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFile(boolean removeDataClass) {
    	if(projectCloneValuesCalculateByFileCache.get(removeDataClass) != null) {
    		return projectCloneValuesCalculateByFileCache.get(removeDataClass);
    	}
    	LOGGER.info("finish groupFileCloneNode");
    	Map<CloneGroup, Map<Project, CloneLineValue<Project>>> result = new HashMap<>();
    	
    	Map<Project, CloneLineValue<Project>> projectToCloneValue = new HashMap<>();
		for(Project project : staticAnalyseService.allProjects()) {
			CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
			projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
			projectToCloneValue.put(project, projectCloneValue);
		}
		result.put(CloneGroup.ALL_CLONE_GROUP_FILE, projectToCloneValue);
    	
    	for(CloneGroup group : basicCloneQueryService.queryGroups(CloneLevel.file)) {
    		projectToCloneValue = new HashMap<>();
    		for(Project project : staticAnalyseService.allProjects()) {
    			CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
    			projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
    			projectToCloneValue.put(project, projectCloneValue);
    		}
    		result.put(group, projectToCloneValue);
    	}
    	Collection<FileCloneGroup> groupFileClones = groupFileClones(removeDataClass);
    	for(FileCloneGroup group : groupFileClones) {
    		for(ProjectFile file : group.getFiles()) {
    			Project project = containRelationService.findFileBelongToProject(file);
    			CloneLineValue<Project> groupProjectCloneValue = result.get(group.getGroup()).get(project);
    			CloneLineValue<Project> allGroupProjectCloneValue = result.get(CloneGroup.ALL_CLONE_GROUP_FILE).get(project);
    			groupProjectCloneValue.addCloneFile(file);
    			allGroupProjectCloneValue.addCloneFile(file);
    			result.get(group.getGroup()).put(project, groupProjectCloneValue);
    			result.get(CloneGroup.ALL_CLONE_GROUP_FILE).put(project, allGroupProjectCloneValue);
    		}
    	}
    	projectCloneValuesCalculateByFileCache.put(removeDataClass, result);
    	return result;
    }
    
    Map<Boolean, Map<CloneGroup, Map<Project, CloneLineValue<Project>>>> projectCloneValuesCalculateByFunctionCache = new ConcurrentHashMap<>();
    @Override
    public Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFunction(boolean removeFileLevelClone) {
    	if(projectCloneValuesCalculateByFunctionCache.get(removeFileLevelClone) != null) {
    		return projectCloneValuesCalculateByFunctionCache.get(removeFileLevelClone);
    	}
    	Map<CloneGroup, Map<Project, CloneLineValue<Project>>> result = new HashMap<>();
    	Map<Project, CloneLineValue<Project>> projectToCloneValue = new HashMap<>();
		for(Project project : staticAnalyseService.allProjects()) {
			CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
			projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
			projectToCloneValue.put(project, projectCloneValue);
		}
		result.put(CloneGroup.ALL_CLONE_GROUP_FUNCTION, projectToCloneValue);
		
    	for(CloneGroup group : basicCloneQueryService.queryGroups(CloneLevel.function)) {
    		projectToCloneValue = new HashMap<>();
    		for(Project project : staticAnalyseService.allProjects()) {
    			CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
    			projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
    			projectToCloneValue.put(project, projectCloneValue);
    		}
    		result.put(group, projectToCloneValue);
    	}
    	Collection<FunctionCloneGroup> groupFunctionClones = groupFunctionClones(removeFileLevelClone);
    	for(FunctionCloneGroup group : groupFunctionClones) {
    		for(Function function : group.getFunctions()) {
    			ProjectFile file = containRelationService.findFunctionBelongToFile(function);
    			Project project = containRelationService.findFileBelongToProject(file);
    			CloneLineValue<Project> groupProjectCloneValue = result.get(group.getGroup()).get(project);
    			CloneLineValue<Project> allGroupProjectCloneValue = result.get(CloneGroup.ALL_CLONE_GROUP_FUNCTION).get(project);
    			groupProjectCloneValue.addCloneFunction(function);
    			allGroupProjectCloneValue.addCloneFunction(function);
    			result.get(group.getGroup()).put(project, groupProjectCloneValue);
    			result.get(CloneGroup.ALL_CLONE_GROUP_FUNCTION).put(project, allGroupProjectCloneValue);
    		}
    	}
    	projectCloneValuesCalculateByFunctionCache.put(removeFileLevelClone, result);
    	return result;
    }
    
    @Override
    public Map<MicroService, CloneLineValue<MicroService>> msCloneLineValuesGroup(Iterable<MicroService> mss, CloneGroup group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass) {
    	Map<MicroService, CloneLineValue<MicroService>> result = new HashMap<>();
    	for(MicroService ms : mss) {
    		result.put(ms, msCloneLineValuesGroup(ms, group, level, removeFileLevelClone, removeDataClass));
    	}
    	return result;
    }
    
    private Map<Boolean, Map<String, Map<Long, CloneLineValue<MicroService>>>> msCloneLineValuesCalculateGroupByFunctionCache = new ConcurrentHashMap<>();
    @Override
    public Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(
    		Collection<MicroService> mss, boolean removeFileLevelClone) {
    	if(msCloneLineValuesCalculateGroupByFunctionCache.get(removeFileLevelClone) != null) {
    		return msCloneLineValuesCalculateGroupByFunctionCache.get(removeFileLevelClone);
    	}
    	Map<String, Map<Long, CloneLineValue<MicroService>>> result = new HashMap<>();
    	Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectGroups = projectCloneLineValuesCalculateGroupByFunction(removeFileLevelClone);
    	LOGGER.info("finish projectCloneLineValuesCalculateGroupByFunction");
    	for(Map.Entry<CloneGroup, Map<Project, CloneLineValue<Project>>> projectGroup : projectGroups.entrySet()) {
    		CloneGroup group = projectGroup.getKey();
    		Map<Project, CloneLineValue<Project>> projectsValue = projectGroup.getValue();
    		Map<Long, CloneLineValue<MicroService>> mssValue = new HashMap<>();
    		for(MicroService ms : mss) {
    			CloneLineValue<MicroService> value = new CloneLineValue<>(ms);
    			Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
    			for(Project project : projects) {
    				CloneLineValue<Project> projectValue = projectsValue.get(project);
    				if(projectValue != null) {
    					value.addAllFiles(projectValue.getAllFiles());
    					value.addAllCloneFiles(projectValue.getCloneFiles());
    					value.addAllCloneFunctions(projectValue.getCloneFunctions());
    				}
    			}
    			mssValue.put(ms.getId(), value);
    		}
    		result.put(group.getName(), mssValue);
    	}
    	msCloneLineValuesCalculateGroupByFunctionCache.put(removeFileLevelClone, result);
    	return result;
    }
    
    Map<Boolean, Map<String, Map<Long, CloneLineValue<MicroService>>>> msCloneLineValuesCalculateGroupByFileCache = new ConcurrentHashMap<>();
    @Override
    public Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(Collection<MicroService> mss, boolean removeDataClass) {
    	if(msCloneLineValuesCalculateGroupByFileCache.get(removeDataClass) != null) {
    		return msCloneLineValuesCalculateGroupByFileCache.get(removeDataClass);
    	}
    	Map<String, Map<Long, CloneLineValue<MicroService>>> result = new HashMap<>();
    	Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectGroups = projectCloneLineValuesCalculateGroupByFile(removeDataClass);
    	LOGGER.info("finish projectCloneLineValuesCalculateGroupByFile");
    	for(Map.Entry<CloneGroup, Map<Project, CloneLineValue<Project>>> projectGroup : projectGroups.entrySet()) {
    		CloneGroup group = projectGroup.getKey();
    		Map<Project, CloneLineValue<Project>> projectsValue = projectGroup.getValue();
    		Map<Long, CloneLineValue<MicroService>> mssValue = new HashMap<>();
    		for(MicroService ms : mss) {
    			CloneLineValue<MicroService> value = new CloneLineValue<>(ms);
    			Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
    			for(Project project : projects) {
    				CloneLineValue<Project> projectValue = projectsValue.get(project);
    				if(projectValue != null) {
    					value.addAllFiles(projectValue.getAllFiles());
    					value.addAllCloneFiles(projectValue.getCloneFiles());
    					value.addAllCloneFunctions(projectValue.getCloneFunctions());
    				}
    			}
    			mssValue.put(ms.getId(), value);
    		}
    		result.put(group.getName(), mssValue);
    	}
    	msCloneLineValuesCalculateGroupByFileCache.put(removeDataClass, result);
    	return result;
    }

    @Override
    public CloneLineValue<MicroService> msCloneLineValuesGroup(MicroService ms, CloneGroup group, CloneLevel level,
    		boolean removeFileLevelClone, boolean removeDataClass) {
    	CloneLineValue<MicroService> result = new CloneLineValue<>(ms);
    	Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectResultsGroup = null;
    	if(level == CloneLevel.function) {
    		projectResultsGroup = projectCloneLineValuesCalculateGroupByFunction(removeFileLevelClone);
    	} else {
    		projectResultsGroup = projectCloneLineValuesCalculateGroupByFile(removeDataClass);
    	}
    	Map<Project, CloneLineValue<Project>> projectResults = projectResultsGroup.get(group);
    	if(projectResults == null) {
    		return result;
    	}
    	Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
    	for(Project project : projects) {
    		CloneLineValue<Project> projectResult = projectResults.get(project);
    		if(projectResult == null) {
    			continue;
    		}
    		result.addAllFiles(projectResult.getAllFiles());
    		result.addAllCloneFiles(projectResult.getCloneFiles());
    		result.addAllCloneFunctions(projectResult.getCloneFunctions());
    	}
    	return result;
    }
    
	@Override
	public Collection<Clone<Project, FileCloneFile>> queryProjectCloneFromFileClone(Iterable<FileCloneFile> fileClones,
			boolean removeSameNode) {
		List<Clone<Project, FileCloneFile>> result = new ArrayList<>();
		Map<Project, Map<Project, Clone<Project, FileCloneFile>>> projectToProjectClones = new HashMap<>();
		for(FileCloneFile fileCloneFile : fileClones) {
			ProjectFile file1= fileCloneFile.getFile1();
			ProjectFile file2 = fileCloneFile.getFile2();
			if(file1.equals(file2)) {
				continue;
			}
			Project project1 = containRelationService.findFileBelongToProject(file1);
			Project project2 = containRelationService.findFileBelongToProject(file2);
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			Clone<Project, FileCloneFile> clone = hasFileCloneInProject(projectToProjectClones, project1, project2);
			if(clone == null) {
				clone = new Clone<Project, FileCloneFile>();
				clone.setNode1(project1);
				clone.setNode2(project2);
				result.add(clone);
			}
			clone.addChild(fileCloneFile);
			
			Map<Project, Clone<Project, FileCloneFile>> project1ToClones = projectToProjectClones.getOrDefault(project1, new HashMap<>());
			project1ToClones.put(project2, clone);
			projectToProjectClones.put(project1, project1ToClones);
		}
		return result;
	}
	
	private Clone<Project, FunctionCloneFunction> hasFunctionCloneInProject(
			Map<Project, Map<Project, Clone<Project, FunctionCloneFunction>>> projectToProjectClones, 
			Project project1, Project project2) {
		Map<Project, Clone<Project, FunctionCloneFunction>> project1ToClones 
			= projectToProjectClones.getOrDefault(project1, new HashMap<>());
		Clone<Project, FunctionCloneFunction> clone = project1ToClones.get(project2);
		if(clone != null) {
			return clone;
		}
		Map<Project, Clone<Project, FunctionCloneFunction>> project2ToClones 
			= projectToProjectClones.getOrDefault(project2, new HashMap<>());
		clone = project2ToClones.get(project1);
		return clone;
	}
	
	private Clone<Project, FileCloneFile> hasFileCloneInProject(
			Map<Project, Map<Project, Clone<Project, FileCloneFile>>> projectToProjectClones, 
			Project project1, Project project2) {
		Map<Project, Clone<Project, FileCloneFile>> project1ToClones 
			= projectToProjectClones.getOrDefault(project1, new HashMap<>());
		Clone<Project, FileCloneFile> clone = project1ToClones.get(project2);
		if(clone != null) {
			return clone;
		}
		Map<Project, Clone<Project, FileCloneFile>> project2ToClones 
			= projectToProjectClones.getOrDefault(project2, new HashMap<>());
		clone = project2ToClones.get(project1);
		return clone;
	}

	@Override
	public Collection<Clone<Project, FunctionCloneFunction>> findProjectCloneFromFunctionClone(
			Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode) {
		List<Clone<Project, FunctionCloneFunction>> result = new ArrayList<>();
		Map<Project, Map<Project, Clone<Project, FunctionCloneFunction>>> projectToProjectClones = new HashMap<>();
		for(FunctionCloneFunction functionCloneFunction : functionClones) {
			Function function1 = functionCloneFunction.getFunction1();
			Function function2 = functionCloneFunction.getFunction2();
			if(function1.equals(function2)) {
				continue;
			}
			Project project1 = containRelationService.findFunctionBelongToProject(function1);
			Project project2 = containRelationService.findFunctionBelongToProject(function2);
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			Clone<Project, FunctionCloneFunction> clone = hasFunctionCloneInProject(projectToProjectClones, project1, project2);
			if(clone == null) {
				clone = new Clone<Project, FunctionCloneFunction>();
				clone.setNode1(project1);
				clone.setNode2(project2);
				result.add(clone);
			}
			// 函数间的克隆作为Children
			clone.addChild(functionCloneFunction);
			
			Map<Project, Clone<Project, FunctionCloneFunction>> project1ToClones 
				= projectToProjectClones.getOrDefault(project1, new HashMap<>());
			project1ToClones.put(project2, clone);
			projectToProjectClones.put(project1, project1ToClones);
		}
		return result;
	}
	
	private Clone<MicroService, FunctionCloneFunction> hasCloneInFunctionClones(
			Map<MicroService, Map<MicroService, Clone<MicroService, FunctionCloneFunction>>> msToMsClones, MicroService ms1, MicroService ms2) {
		Map<MicroService, Clone<MicroService, FunctionCloneFunction>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
		Clone<MicroService, FunctionCloneFunction> clone = ms1ToClones.get(ms2);
		if(clone != null) {
			return clone;
		}
		Map<MicroService, Clone<MicroService, FunctionCloneFunction>> ms2ToClones = msToMsClones.getOrDefault(ms2, new HashMap<>());
		clone = ms2ToClones.get(ms1);
		return clone;
	}
	
	private Clone<MicroService, FileCloneFile> hasCloneInFileClones(
			Map<MicroService, Map<MicroService, Clone<MicroService, FileCloneFile>>> msToMsClones, MicroService ms1, MicroService ms2) {
		Map<MicroService, Clone<MicroService, FileCloneFile>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
		Clone<MicroService, FileCloneFile> clone = ms1ToClones.get(ms2);
		if(clone != null) {
			return clone;
		}
		Map<MicroService, Clone<MicroService, FileCloneFile>> ms2ToClones = msToMsClones.getOrDefault(ms2, new HashMap<>());
		clone = ms2ToClones.get(ms1);
		return clone;
	}

	@Override
	public Collection<Clone<MicroService, FunctionCloneFunction>> findMicroServiceCloneFromFunctionClone(Iterable<FunctionCloneFunction> functionClones, boolean removeSameNode) {
		Collection<Clone<Project, FunctionCloneFunction>> projectClones = findProjectCloneFromFunctionClone(functionClones, removeSameNode);
		List<Clone<MicroService, FunctionCloneFunction>> result = new ArrayList<>();
		Map<MicroService, Map<MicroService, Clone<MicroService, FunctionCloneFunction>>> msToMsClones = new HashMap<>();
		for(Clone<Project, FunctionCloneFunction> projectClone : projectClones) {
			Project project1 = projectClone.getNode1();
			Project project2 = projectClone.getNode2();
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms1 == null || ms2 == null) {
				continue;
			}
			if(removeSameNode && ms1.equals(ms2)) {
				continue;
			}
			Clone<MicroService, FunctionCloneFunction> clone = hasCloneInFunctionClones(msToMsClones, ms1, ms2);
			if(clone == null) {
				clone = new Clone<MicroService, FunctionCloneFunction>();
				clone.setNode1(ms1);
				clone.setNode2(ms2);
				result.add(clone);
				CloneValueCalculatorForMicroService calculator = new CloneValueCalculatorForMicroService();
				Iterable<Function> functions1 = containRelationService.findMicroServiceContainFunctions(ms1);
				Iterable<Function> functions2 = containRelationService.findMicroServiceContainFunctions(ms2);
				calculator.addFunctions(functions1, ms1);
				calculator.addFunctions(functions2, ms2);
				clone.setCalculator(calculator);
			}
			clone.addChildren(projectClone.getChildren());
			Map<MicroService, Clone<MicroService, FunctionCloneFunction>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
			ms1ToClones.put(ms2, clone);
			msToMsClones.put(ms1, ms1ToClones);
			
		}
		return result;
	}

	@Override
	public Collection<Clone<MicroService, FileCloneFile>> findMicroServiceCloneFromFileClone(
			Iterable<FileCloneFile> fileClones, boolean removeSameNode) {
		Iterable<Clone<Project, FileCloneFile>> projectClones = queryProjectCloneFromFileClone(fileClones, removeSameNode);
		List<Clone<MicroService, FileCloneFile>> result = new ArrayList<>();
		Map<MicroService, Map<MicroService, Clone<MicroService, FileCloneFile>>> msToMsClones = new HashMap<>();
		for(Clone<Project, FileCloneFile> projectClone : projectClones) {
			Project project1 = projectClone.getNode1();
			Project project2 = projectClone.getNode2();
			if(removeSameNode && project1.equals(project2)) {
				continue;
			}
			MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms1 == null || ms2 == null) {
				continue;
			}
			if(removeSameNode && ms1.equals(ms2)) {
				continue;
			}
			Clone<MicroService, FileCloneFile> clone = hasCloneInFileClones(msToMsClones, ms1, ms2);
			if(clone == null) {
				clone = new Clone<MicroService, FileCloneFile>();
				clone.setNode1(ms1);
				clone.setNode2(ms2);
				result.add(clone);
				CloneValueCalculatorForMicroService calculator = new CloneValueCalculatorForMicroService();
				Iterable<Function> functions1 = containRelationService.findMicroServiceContainFunctions(ms1);
				Iterable<Function> functions2 = containRelationService.findMicroServiceContainFunctions(ms2);
				calculator.addFunctions(functions1, ms1);
				calculator.addFunctions(functions2, ms2);
				clone.setCalculator(calculator);
			}
			clone.addChildren(projectClone.getChildren());
			Map<MicroService, Clone<MicroService, FileCloneFile>> ms1ToClones = msToMsClones.getOrDefault(ms1, new HashMap<>());
			ms1ToClones.put(ms2, clone);
			msToMsClones.put(ms1, ms1ToClones);
			
		}
		return result;
	}

	private Map<CloneLevel, Map<Boolean, Collection<MicroService>>> msSortByMsCloneLineCountCache = new ConcurrentHashMap<>();
	@Override
	public Collection<MicroService> msSortByMsCloneLineCount(Collection<MicroService> mss, CloneLevel level, 
			boolean removeFileLevelClone, boolean removeDataClass) {
		Map<Boolean, Collection<MicroService>> mapCache = msSortByMsCloneLineCountCache.get(level);
		if(mapCache != null) {
			if(CloneLevel.function == level) {
				Collection<MicroService> cache = mapCache.get(removeFileLevelClone);
				if(cache != null && !cache.isEmpty()) {
					return cache;
				}
			} else {
				Collection<MicroService> cache = mapCache.get(removeDataClass);
				if(cache != null && !cache.isEmpty()) {
					return cache;
				}
			}
		}
		mapCache = new ConcurrentHashMap<>();
		
		List<MicroService> result = new ArrayList<>(mss);
		Map<String, Map<Long, CloneLineValue<MicroService>>> temp = new HashMap<>();
		Map<MicroService, Integer> msCount = new HashMap<>();
		if(level == CloneLevel.function) {
			temp = msCloneLineValuesCalculateGroupByFunction(mss, removeFileLevelClone);
		} else {
			temp = msCloneLineValuesCalculateGroupByFile(mss, removeDataClass);
		}
		for(String group : temp.keySet()) {
			Map<Long, CloneLineValue<MicroService>> values = temp.get(group);
			for(MicroService ms : mss) {
				CloneLineValue<MicroService> value = values.getOrDefault(ms.getId(), new CloneLineValue<MicroService>(ms));
				if(level == CloneLevel.function) {
					if(!value.getCloneFunctions().isEmpty()) {
						int count = msCount.getOrDefault(ms, 0) + 1;
						msCount.put(ms, count);
					}
				} else {
					if(!value.getCloneFiles().isEmpty()) {
						int count = msCount.getOrDefault(ms, 0) + 1;
						msCount.put(ms, count);
					}
				}
			}
		}
		result.sort((ms1, ms2) -> {
			return msCount.getOrDefault(ms2, 0) - msCount.getOrDefault(ms1, 0);
		});
		if(CloneLevel.function == level) {
			mapCache.put(removeFileLevelClone, result);
		} else {
			mapCache.put(removeDataClass, result);
		}
		msSortByMsCloneLineCountCache.put(level, mapCache);
		return result;
	}

	@Override
	public boolean isCloneBetweenFiles(ProjectFile file1, ProjectFile file2) {
		CloneGroup group1 = containRelationService.findFileBelongToCloneGroup(file1);
		if(group1 == null) {
			return false;
		}
		CloneGroup group2 = containRelationService.findFileBelongToCloneGroup(file2);
		if(group2 == null) {
			return false;
		}
		return group1.equals(group2);
	}
	
	public Collection<MicroService> fileCloneGroupContainMSs(FileCloneGroup group) {
		Set<MicroService> result = new HashSet<>();
		Collection<Project> fileCloneGroupContainProjects = fileCloneGroupContainProjects(group);
		for(Project project : fileCloneGroupContainProjects) {
			result.add(containRelationService.findProjectBelongToMicroService(project));
		}
		return result;
	}
	
	public Collection<MicroService> functionCloneGroupContainMSs(FunctionCloneGroup group) {
		Set<MicroService> result = new HashSet<>();
		Collection<Project> functionCloneGroupContainProjects = functionCloneGroupContainProjects(group);
		for(Project project : functionCloneGroupContainProjects) {
			result.add(containRelationService.findProjectBelongToMicroService(project));
		}
		return result;
	}
	
	private Map<FileCloneGroup, Collection<Project>> fileCloneGroupContainProjectsCache = new ConcurrentHashMap<>();
	public Collection<Project> fileCloneGroupContainProjects(FileCloneGroup group) {
		if(fileCloneGroupContainProjectsCache.get(group) != null) {
			return fileCloneGroupContainProjectsCache.get(group);
		}
		Set<Project> result = new HashSet<>();
		for(ProjectFile file : group.getFiles()) {
			Project project = containRelationService.findFileBelongToProject(file);
			result.add(project);
		}
		fileCloneGroupContainProjectsCache.put(group, result);
		return result;
	}
	
	private Map<FunctionCloneGroup, Collection<Project>> functionCloneGroupContainProjectsCache = new ConcurrentHashMap<>();
	public Collection<Project> functionCloneGroupContainProjects(FunctionCloneGroup group) {
		if(functionCloneGroupContainProjectsCache.get(group) != null) {
			return functionCloneGroupContainProjectsCache.get(group);
		}
		Set<Project> result = new HashSet<>();
		for(Function function : group.getFunctions()) {
			Project project = containRelationService.findFunctionBelongToProject(function);
			result.add(project);
		}
		functionCloneGroupContainProjectsCache.put(group, result);
		return result;
	}

	@Override
	public Collection<FileCloneGroup> groupFileClonesContainProjects(Collection<FileCloneGroup> groups,
			Collection<Project> projects) {
		Set<FileCloneGroup> result = new HashSet<>();
		for(FileCloneGroup group : groups) {
			Collection<Project> containProjects = fileCloneGroupContainProjects(group);
			boolean contain = true;
			for(Project p : projects) {
				if(!containProjects.contains(p)) {
					contain = false;
					break;
				}
			}
			if(contain) {
				result.add(group);
			}
		}
		return result;
	}

	@Override
	public Collection<FunctionCloneGroup> groupFunctionClonesContainProjects(Collection<FunctionCloneGroup> groups,
			Collection<Project> projects) {
		Set<FunctionCloneGroup> result = new HashSet<>();
		for(FunctionCloneGroup group : groups) {
			Collection<Project> containProjects = functionCloneGroupContainProjects(group);
			boolean contain = true;
			for(Project p : projects) {
				if(!containProjects.contains(p)) {
					contain = false;
					break;
				}
			}
			if(contain) {
				result.add(group);
			}
		}
		return result;
	}

	@Override
	public Collection<FileCloneGroup> groupFileClonesContainMSs(Collection<FileCloneGroup> groups,
			Collection<MicroService> mss) {
		Collection<Project> projects = new ArrayList<>();
		for(MicroService ms : mss) {
			projects.addAll(containRelationService.findMicroServiceContainProjects(ms));
		}
		return groupFileClonesContainProjects(groups, projects);
	}

	@Override
	public Collection<FunctionCloneGroup> groupFunctionClonesContainMSs(Collection<FunctionCloneGroup> groups,
			Collection<MicroService> mss) {
		Collection<Project> projects = new ArrayList<>();
		for(MicroService ms : mss) {
			projects.addAll(containRelationService.findMicroServiceContainProjects(ms));
		}
		return groupFunctionClonesContainProjects(groups, projects);
	}
	
}
