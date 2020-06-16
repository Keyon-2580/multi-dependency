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

import cn.edu.fudan.se.multidependency.model.Language;
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
	private NodeService nodeService;
	
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
	private Collection<FileCloneGroup> groupFileClones(boolean removeDataClass) {
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
			return group2.getNodes().size() - group1.getNodes().size();
		});
		groupFileCloneRelationCache.put(removeDataClass, result);
		return result;
    }

	private Map<Language, Map<Boolean, Collection<FileCloneGroup>>> languageToFileGroup = new ConcurrentHashMap<>();
    @Override
	public Collection<FileCloneGroup> groupFileClones(boolean removeDataClass, Language language) {
		Collection<FileCloneGroup> allFileGroups = groupFileClones(removeDataClass);
		if(language == null) {
			return allFileGroups;
		}
		if(languageToFileGroup.get(language) != null && languageToFileGroup.get(language).get(removeDataClass) != null) {
    		return languageToFileGroup.get(language).get(removeDataClass);
    	}
		Collection<FileCloneGroup> result = new ArrayList<>();
		for(FileCloneGroup group : allFileGroups) {
			for(ProjectFile file : group.getNodes()) {
				Project project = containRelationService.findFileBelongToProject(file);
				if(project.getLanguage().equals(language.toString())) {
					result.add(group);
				}
				break;
			}
		}
		Map<Boolean, Collection<FileCloneGroup>> temp = new ConcurrentHashMap<>();
		temp.put(removeDataClass, result);
		languageToFileGroup.put(language, temp);
		return result;
	}
	
	Map<Boolean, Collection<FunctionCloneGroup>> groupFunctionCloneRelationCache = new ConcurrentHashMap<>();
	private Collection<FunctionCloneGroup> groupFunctionClones(boolean removeFileClone) {
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
			return group2.getNodes().size() - group1.getNodes().size();
		});
		groupFunctionCloneRelationCache.put(removeFileClone, result);
		return result;
	}
    
	private Map<Language, Map<Boolean, Collection<FunctionCloneGroup>>> languageToFunctionGroup = new ConcurrentHashMap<>();
    @Override
	public Collection<FunctionCloneGroup> groupFunctionClones(boolean removeFileClone, Language language) {
    	Collection<FunctionCloneGroup> allFunctionGroups = groupFunctionClones(removeFileClone);
    	if(language == null) {
    		return allFunctionGroups;
    	}
    	if(languageToFunctionGroup.get(language) != null && languageToFunctionGroup.get(language).get(removeFileClone) != null) {
    		return languageToFunctionGroup.get(language).get(removeFileClone);
    	}
		Collection<FunctionCloneGroup> result = new ArrayList<>();
		for(FunctionCloneGroup group : allFunctionGroups) {
			for(Function function : group.getNodes()) {
				Project project = containRelationService.findFunctionBelongToProject(function);
				if(project.getLanguage().equals(language.toString())) {
					result.add(group);
				}
				break;
			}
		}
		Map<Boolean, Collection<FunctionCloneGroup>> temp = new ConcurrentHashMap<>();
		temp.put(removeFileClone, result);
		languageToFunctionGroup.put(language, temp);
		return result;
	}
    
    private Map<String, Map<Long, CloneLineValue<Project>>> projectCloneLineValuesCalculate(CloneLevel level, boolean removeDataClass, boolean removeFileClone, Language language) {
    	Map<String, Map<Long, CloneLineValue<Project>>> result = new HashMap<>();
    	Iterable<Project> projects = staticAnalyseService.allProjects();
    	if(language != null) {
    		projects = nodeService.queryProjects(language);
    	}
    	Map<Long, CloneLineValue<Project>> projectToCloneValue = new HashMap<>();
		for(Project project : projects) {
			CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
			projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
			projectToCloneValue.put(project.getId(), projectCloneValue);
		}
		result.put(CloneGroup.ALL_CLONE_GROUP_FUNCTION.getName(), projectToCloneValue);
		
    	for(CloneGroup group : basicCloneQueryService.queryGroups(level)) {
    		projectToCloneValue = new HashMap<>();
    		for(Project project : projects) {
    			CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
    			projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
    			projectToCloneValue.put(project.getId(), projectCloneValue);
    		}
    		result.put(group.getName(), projectToCloneValue);
    	}
    	if(CloneLevel.function == level) {
    		Collection<FunctionCloneGroup> groupFunctionClones = groupFunctionClones(removeFileClone, language);
    		for(FunctionCloneGroup group : groupFunctionClones) {
    			for(Function function : group.getNodes()) {
    				ProjectFile file = containRelationService.findFunctionBelongToFile(function);
    				Project project = containRelationService.findFileBelongToProject(file);
    				CloneLineValue<Project> groupProjectCloneValue = result.get(group.getGroup().getName()).get(project.getId());
    				CloneLineValue<Project> allGroupProjectCloneValue = result.get(CloneGroup.ALL_CLONE_GROUP_FUNCTION.getName()).get(project.getId());
    				groupProjectCloneValue.addCloneFunction(function);
    				allGroupProjectCloneValue.addCloneFunction(function);
    				result.get(group.getGroup().getName()).put(project.getId(), groupProjectCloneValue);
    				result.get(CloneGroup.ALL_CLONE_GROUP_FUNCTION.getName()).put(project.getId(), allGroupProjectCloneValue);
    			}
    		}
    	} else {
    		Collection<FileCloneGroup> groupFileClones = groupFileClones(removeDataClass, language);
        	for(FileCloneGroup group : groupFileClones) {
        		for(ProjectFile file : group.getNodes()) {
        			Project project = containRelationService.findFileBelongToProject(file);
        			CloneLineValue<Project> groupProjectCloneValue = result.get(group.getGroup().getName()).get(project.getId());
        			CloneLineValue<Project> allGroupProjectCloneValue = result.get(CloneGroup.ALL_CLONE_GROUP_FILE.getName()).get(project.getId());
        			groupProjectCloneValue.addCloneFile(file);
        			allGroupProjectCloneValue.addCloneFile(file);
        			result.get(group.getGroup().getName()).put(project.getId(), groupProjectCloneValue);
        			result.get(CloneGroup.ALL_CLONE_GROUP_FILE.getName()).put(project.getId(), allGroupProjectCloneValue);
        		}
        	}
    	}
    	return result;
    }
    
  	Map<Boolean, Map<String, Map<Long, CloneLineValue<Project>>>> allProjectCloneValuesCalculateByFunctionCache = new ConcurrentHashMap<>();
  	Map<Boolean, Map<String, Map<Long, CloneLineValue<Project>>>> javaProjectCloneValuesCalculateByFunctionCache = new ConcurrentHashMap<>();
  	Map<Boolean, Map<String, Map<Long, CloneLineValue<Project>>>> cppProjectCloneValuesCalculateByFunctionCache = new ConcurrentHashMap<>();
    @Override
    public Map<String, Map<Long, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFunction(boolean removeFileLevelClone, Language language) {
    	Map<Boolean, Map<String, Map<Long, CloneLineValue<Project>>>> cache = null;
    	if(language == null) {
    		cache = allProjectCloneValuesCalculateByFunctionCache;
    	} else if(Language.cpp == language) {
    		cache = javaProjectCloneValuesCalculateByFunctionCache;
    	} else {
    		cache = cppProjectCloneValuesCalculateByFunctionCache;
    	}
    	if(cache.get(removeFileLevelClone) != null) {
    		return cache.get(removeFileLevelClone);
    	}
    	Map<String, Map<Long, CloneLineValue<Project>>> result = projectCloneLineValuesCalculate(CloneLevel.function, false, removeFileLevelClone, language);
    	cache.put(removeFileLevelClone, result);
    	return result;
    }

  	Map<Boolean, Map<String, Map<Long, CloneLineValue<Project>>>> allProjectCloneValuesCalculateByFileCache = new ConcurrentHashMap<>();
  	Map<Boolean, Map<String, Map<Long, CloneLineValue<Project>>>> javaProjectCloneValuesCalculateByFileCache = new ConcurrentHashMap<>();
  	Map<Boolean, Map<String, Map<Long, CloneLineValue<Project>>>> cppProjectCloneValuesCalculateByFileCache = new ConcurrentHashMap<>();
    @Override
    public Map<String, Map<Long, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFile(boolean removeDataClass, Language language) {
    	Map<Boolean, Map<String, Map<Long, CloneLineValue<Project>>>> cache = null;
    	if(language == null) {
    		cache = allProjectCloneValuesCalculateByFileCache;
    	} else if(Language.cpp == language) {
    		cache = cppProjectCloneValuesCalculateByFileCache;
    	} else {
    		cache = javaProjectCloneValuesCalculateByFileCache;
    	}
    	if(cache.get(removeDataClass) != null) {
    		return cache.get(removeDataClass);
    	}
    	Map<String, Map<Long, CloneLineValue<Project>>> result = projectCloneLineValuesCalculate(CloneLevel.file, removeDataClass, false, language);
    	cache.put(removeDataClass, result);
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
    public Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(boolean removeFileLevelClone) {
    	if(msCloneLineValuesCalculateGroupByFunctionCache.get(removeFileLevelClone) != null) {
    		return msCloneLineValuesCalculateGroupByFunctionCache.get(removeFileLevelClone);
    	}
    	Collection<MicroService> mss = msService.findAllMicroService();
    	Map<String, Map<Long, CloneLineValue<MicroService>>> result = new HashMap<>();
    	Map<String, Map<Long, CloneLineValue<Project>>> projectGroups = projectCloneLineValuesCalculateGroupByFunction(removeFileLevelClone, null);
    	LOGGER.info("finish projectCloneLineValuesCalculateGroupByFunction");
    	for(Map.Entry<String, Map<Long, CloneLineValue<Project>>> projectGroup : projectGroups.entrySet()) {
    		String group = projectGroup.getKey();
    		Map<Long, CloneLineValue<Project>> projectsValue = projectGroup.getValue();
    		Map<Long, CloneLineValue<MicroService>> mssValue = new HashMap<>();
    		for(MicroService ms : mss) {
    			CloneLineValue<MicroService> value = new CloneLineValue<>(ms);
    			Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
    			for(Project project : projects) {
    				CloneLineValue<Project> projectValue = projectsValue.get(project.getId());
    				if(projectValue != null) {
    					value.addAllFiles(projectValue.getAllFiles());
    					value.addAllCloneFiles(projectValue.getCloneFiles());
    					value.addAllCloneFunctions(projectValue.getCloneFunctions());
    				}
    			}
    			mssValue.put(ms.getId(), value);
    		}
    		result.put(group, mssValue);
    	}
    	msCloneLineValuesCalculateGroupByFunctionCache.put(removeFileLevelClone, result);
    	return result;
    }
    
    Map<Boolean, Map<String, Map<Long, CloneLineValue<MicroService>>>> msCloneLineValuesCalculateGroupByFileCache = new ConcurrentHashMap<>();
    @Override
    public Map<String, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(boolean removeDataClass) {
    	if(msCloneLineValuesCalculateGroupByFileCache.get(removeDataClass) != null) {
    		return msCloneLineValuesCalculateGroupByFileCache.get(removeDataClass);
    	}
    	Collection<MicroService> mss = msService.findAllMicroService();
    	Map<String, Map<Long, CloneLineValue<MicroService>>> result = new HashMap<>();
    	Map<String, Map<Long, CloneLineValue<Project>>> projectGroups = projectCloneLineValuesCalculateGroupByFile(removeDataClass, null);
    	LOGGER.info("finish projectCloneLineValuesCalculateGroupByFile");
    	for(Map.Entry<String, Map<Long, CloneLineValue<Project>>> projectGroup : projectGroups.entrySet()) {
    		String group = projectGroup.getKey();
    		Map<Long, CloneLineValue<Project>> projectsValue = projectGroup.getValue();
    		Map<Long, CloneLineValue<MicroService>> mssValue = new HashMap<>();
    		for(MicroService ms : mss) {
    			CloneLineValue<MicroService> value = new CloneLineValue<>(ms);
    			Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
    			for(Project project : projects) {
    				CloneLineValue<Project> projectValue = projectsValue.get(project.getId());
    				if(projectValue != null) {
    					value.addAllFiles(projectValue.getAllFiles());
    					value.addAllCloneFiles(projectValue.getCloneFiles());
    					value.addAllCloneFunctions(projectValue.getCloneFunctions());
    				}
    			}
    			mssValue.put(ms.getId(), value);
    		}
    		result.put(group, mssValue);
    	}
    	msCloneLineValuesCalculateGroupByFileCache.put(removeDataClass, result);
    	return result;
    }

    @Override
    public CloneLineValue<MicroService> msCloneLineValuesGroup(MicroService ms, CloneGroup group, CloneLevel level,
    		boolean removeFileLevelClone, boolean removeDataClass) {
    	CloneLineValue<MicroService> result = new CloneLineValue<>(ms);
    	Map<String, Map<Long, CloneLineValue<Project>>> projectResultsGroup = null;
    	if(level == CloneLevel.function) {
    		projectResultsGroup = projectCloneLineValuesCalculateGroupByFunction(removeFileLevelClone, null);
    	} else {
    		projectResultsGroup = projectCloneLineValuesCalculateGroupByFile(removeDataClass, null);
    	}
    	Map<Long, CloneLineValue<Project>> projectResults = projectResultsGroup.get(group.getName());
    	if(projectResults == null) {
    		return result;
    	}
    	Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
    	for(Project project : projects) {
    		CloneLineValue<Project> projectResult = projectResults.get(project.getId());
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
	public Collection<MicroService> msSortByMsCloneLineCount(CloneLevel level, 
			boolean removeFileLevelClone, boolean removeDataClass) {
		Collection<MicroService> mss = msService.findAllMicroService();
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
			temp = msCloneLineValuesCalculateGroupByFunction(removeFileLevelClone);
		} else {
			temp = msCloneLineValuesCalculateGroupByFile(removeDataClass);
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
		Collection<Project> fileCloneGroupContainProjects = fileCloneGroupContainProjects(group, null);
		for(Project project : fileCloneGroupContainProjects) {
			result.add(containRelationService.findProjectBelongToMicroService(project));
		}
		return result;
	}
	
	public Collection<MicroService> functionCloneGroupContainMSs(FunctionCloneGroup group) {
		Set<MicroService> result = new HashSet<>();
		Collection<Project> functionCloneGroupContainProjects = functionCloneGroupContainProjects(group, null);
		for(Project project : functionCloneGroupContainProjects) {
			result.add(containRelationService.findProjectBelongToMicroService(project));
		}
		return result;
	}
	
	private Map<FileCloneGroup, Collection<Project>> fileCloneGroupContainProjectsCache = new ConcurrentHashMap<>();
	public Collection<Project> fileCloneGroupContainProjects(FileCloneGroup group, Language language) {
		if(fileCloneGroupContainProjectsCache.get(group) != null) {
			return fileCloneGroupContainProjectsCache.get(group);
		}
		Set<Project> result = new HashSet<>();
		for(ProjectFile file : group.getNodes()) {
			Project project = containRelationService.findFileBelongToProject(file);
			if(language == null) {
				result.add(project);
			} else {
				if(project.getLanguage().equals(language.toString())) {
					result.add(project);
				}
			}
		}
		fileCloneGroupContainProjectsCache.put(group, result);
		return result;
	}
	
	private Map<FunctionCloneGroup, Collection<Project>> functionCloneGroupContainProjectsCache = new ConcurrentHashMap<>();
	public Collection<Project> functionCloneGroupContainProjects(FunctionCloneGroup group, Language language) {
		if(functionCloneGroupContainProjectsCache.get(group) != null) {
			return functionCloneGroupContainProjectsCache.get(group);
		}
		Set<Project> result = new HashSet<>();
		for(Function function : group.getNodes()) {
			Project project = containRelationService.findFunctionBelongToProject(function);
			if(language == null) {
				result.add(project);
			} else {
				if(project.getLanguage().equals(language.toString())) {
					result.add(project);
				}
			}
		}
		functionCloneGroupContainProjectsCache.put(group, result);
		return result;
	}

	@Override
	public Collection<FileCloneGroup> groupFileClonesContainProjects(Collection<FileCloneGroup> groups,
			Collection<Project> projects) {
		Set<FileCloneGroup> result = new HashSet<>();
		for(FileCloneGroup group : groups) {
			Collection<Project> containProjects = fileCloneGroupContainProjects(group, null);
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
			Collection<Project> containProjects = functionCloneGroupContainProjects(group, null);
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

	@Override
	public String exportCloneMicroService(Map<String, Map<Long, CloneLineValue<MicroService>>> data,
											 Collection<MicroService> microservices, CloneLevel level) {
		final String CSV_COLUMN_SEPARATOR = ",";
		final String CSV_ROW_SEPARATOR = "\r\n";
		StringBuffer buf = new StringBuffer();
		buf.append(" ").append(CSV_COLUMN_SEPARATOR);
		for (MicroService microService : microservices) {
			buf.append(microService.getName()).append(CSV_COLUMN_SEPARATOR);
		}
		buf.append(CSV_ROW_SEPARATOR);
		for (Map.Entry<String, Map<Long, CloneLineValue<MicroService>>> group : data.entrySet()) {
			buf.append(group.getKey()).append(CSV_COLUMN_SEPARATOR);
			Map<Long, CloneLineValue<MicroService>> map = group.getValue();
			for (MicroService microService : microservices) {
				CloneLineValue<MicroService> clv = map.get(microService.getId());
				boolean hasData = false;
				buf.append("\"");
				if(level == CloneLevel.function) {
					for (Function function : clv.getCloneFunctions()) {
						hasData = true;
						buf.append(function.getName()).append(CSV_ROW_SEPARATOR);
					}
				} else {
					for (ProjectFile projectFile : clv.getCloneFiles()) {
						hasData = true;
						buf.append(projectFile.getPath()).append(CSV_ROW_SEPARATOR);
					}
				}
				if (hasData) {
					int len = buf.length();
					if (len > 2) buf.delete(len-2, len);
				}
				buf.append("\"");
				buf.append(CSV_COLUMN_SEPARATOR);
			}
			buf.append(CSV_ROW_SEPARATOR);
		}
		return buf.toString();
	}

	@Override
	public String exportCloneProject(Map<String, Map<Long, CloneLineValue<Project>>> data,
											 Collection<Project> projects, CloneLevel level) {
		final String CSV_COLUMN_SEPARATOR = ",";
		final String CSV_ROW_SEPARATOR = "\r\n";
		StringBuffer buf = new StringBuffer();
		buf.append(" ").append(CSV_COLUMN_SEPARATOR);
		for (Project project : projects) {
			buf.append(project.getName()).append("(").append(project.getLanguage().toString()).append(")").append(CSV_COLUMN_SEPARATOR);
		}
		buf.append(CSV_ROW_SEPARATOR);
		for (Map.Entry<String, Map<Long, CloneLineValue<Project>>> group : data.entrySet()) {
			buf.append(group.getKey()).append(CSV_COLUMN_SEPARATOR);
			Map<Long, CloneLineValue<Project>> map = group.getValue();
			for (Project project : projects) {
				CloneLineValue<Project> clv = map.get(project.getId());
				boolean hasData = false;
				buf.append("\"");
				if(level == CloneLevel.function) {
					for (Function function : clv.getCloneFunctions()) {
						hasData = true;
						buf.append(function.getName()).append(CSV_ROW_SEPARATOR);
					}
				} else {
					for (ProjectFile projectFile : clv.getCloneFiles()) {
						hasData = true;
						buf.append(projectFile.getPath()).append(CSV_ROW_SEPARATOR);
					}
				}
				if (hasData) {
					int len = buf.length();
					if (len > 2) buf.delete(len-2, len);
				}
				buf.append("\"");
				buf.append(CSV_COLUMN_SEPARATOR);
			}
			buf.append(CSV_ROW_SEPARATOR);
		}
		return buf.toString();
	}
}
