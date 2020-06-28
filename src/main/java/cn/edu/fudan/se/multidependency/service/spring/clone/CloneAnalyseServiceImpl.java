package cn.edu.fudan.se.multidependency.service.spring.clone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.service.spring.BasicCloneQueryService;
import cn.edu.fudan.se.multidependency.service.spring.CacheService;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.MicroserviceService;
import cn.edu.fudan.se.multidependency.service.spring.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;

@Service
public class CloneAnalyseServiceImpl implements CloneAnalyseService {
    @Autowired
    CloneRepository cloneRepository;
    
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

	@Override
	public CloneGroup addNodeAndRelationToCloneGroup(CloneGroup group) {
		Collection<Clone> clones = basicCloneQueryService.findGroupContainCloneRelations(group);
		for(Clone clone : clones) {
			group.addRelation(clone);
			group.addNode(clone.getCodeNode1());
			group.addNode(clone.getCodeNode2());
		}
		return group;
	}
    
    @Override
	public Collection<CloneGroup> group(CloneRelationType cloneRelationType, Collection<Predicate<CloneGroup>> predicates) {
    	Collection<CloneGroup> groups = basicCloneQueryService.findGroupsContainCloneTypeRelation(cloneRelationType);
    	List<CloneGroup> result = new LinkedList<>(groups);
    	for(Predicate<CloneGroup> pre : predicates) {
    		result.removeIf(pre);
    	}
    	for(CloneGroup group : result) {
    		Collection<Clone> clones = basicCloneQueryService.findGroupContainCloneRelations(group);
    		for(Clone clone : clones) {
    			group.addRelation(clone);
    			group.addNode(clone.getCodeNode1());
    			group.addNode(clone.getCodeNode2());
    		}
    	}
    	return result;
	}

	private Map<CloneGroup, Collection<Project>> cloneGroupContainProjectsCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Project> cloneGroupContainProjects(CloneGroup group) {
		if(cloneGroupContainProjectsCache.get(group) != null) {
			return cloneGroupContainProjectsCache.get(group);
		}
		Set<Project> result = new HashSet<>();
		for(CodeNode node : group.getNodes()) {
			Project project = containRelationService.findCodeNodeBelongToProject(node);
			result.add(project);
		}
		cloneGroupContainProjectsCache.put(group, result);
		return result;
	}
	
	@Override
	public Collection<MicroService> cloneGroupContainMicroServices(CloneGroup group) {
		Set<MicroService> result = new HashSet<>();
		Collection<Project> projects = cloneGroupContainProjects(group);
		for(Project project : projects) {
			MicroService ms = containRelationService.findProjectBelongToMicroService(project);
			if(ms != null) {
				result.add(ms);
			}
		}
		return result;
	}

	@Override
	public Collection<CloneGroup> findGroupsContainProjects(Collection<CloneGroup> groups, Collection<Project> projects) {
		Set<CloneGroup> result = new HashSet<>();
		for(CloneGroup group : groups) {
			Collection<Project> containProjects = cloneGroupContainProjects(group);
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
	public Collection<CloneGroup> findGroupsContainMicroServices(Collection<CloneGroup> groups, Collection<MicroService> mss) {
		Collection<Project> projects = new ArrayList<>();
		for(MicroService ms : mss) {
			projects.addAll(containRelationService.findMicroServiceContainProjects(ms));
		}
		return findGroupsContainProjects(groups, projects);
	}

	@Override
	public Collection<Project> cloneGroupContainProjects(Collection<CloneGroup> groups) {
		Set<Project> result = new HashSet<>();
		for(CloneGroup group : groups) {
			result.addAll(cloneGroupContainProjects(group));
		}
		return result;
	}

	@Override
	public Collection<MicroService> cloneGroupContainMicroServices(Collection<CloneGroup> groups) {
		Set<MicroService> result = new HashSet<>();
		for(CloneGroup group : groups) {
			result.addAll(cloneGroupContainMicroServices(group));
		}
		return result;
	}
	
	@Override
	public Map<CloneGroup, Map<Project, CloneLineValue<Project>>> projectCloneLineValues(Collection<CloneGroup> groups) {
		Iterable<Project> allProjects = staticAnalyseService.allProjects();
		Map<CloneGroup, Map<Project, CloneLineValue<Project>>> result = new HashMap<>();
		for(CloneGroup group : groups) {
			Map<Project, CloneLineValue<Project>> temp = new HashMap<>();
			for(Project project : allProjects) {
				CloneLineValue<Project> projectValue = new CloneLineValue<>(project);
				projectValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
				
				temp.put(project, projectValue);
			}
			result.put(group, temp);
		}
		return result;
	}
	
  	/*Map<Boolean, Map<String, Map<Long, CloneLineValue<Project>>>> allProjectCloneValuesCalculateByFileCache = new ConcurrentHashMap<>();
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
	}*/

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
	
	/*@Override
	public String exportCloneMicroService(Map<String, Map<Long, CloneLineValue<MicroService>>> data,
											 Collection<MicroService> microservices, CloneRelationType cloneRelationType) {
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
											 Collection<Project> projects, CloneRelationType cloneRelationType) {
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
	}*/
}
