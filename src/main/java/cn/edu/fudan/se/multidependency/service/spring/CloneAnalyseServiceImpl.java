package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneRelationNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FileCloneFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FunctionCloneFunctionRepository;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneLineValue;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneValueCalculatorForMicroService;

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
    
    @Override
    public Map<MicroService, CloneLineValue<MicroService>> msCloneLineValuesGroup(Iterable<MicroService> mss, int group, CloneLevel level, boolean removeFileLevelClone, boolean removeDataClass) {
    	Map<MicroService, CloneLineValue<MicroService>> result = new HashMap<>();
    	for(MicroService ms : mss) {
    		result.put(ms, msCloneLineValuesGroup(ms, group, level, removeFileLevelClone, removeDataClass));
    	}
    	return result;
    }
    
    @Override
    public Map<MicroService, CloneLineValue<MicroService>> msCloneLineValues(Iterable<MicroService> mss) {
    	Map<MicroService, CloneLineValue<MicroService>> result = new HashMap<>();
    	Map<Project, CloneLineValue<Project>> projectResults = projectCloneLineValues();
    	for(MicroService ms : mss) {
    		CloneLineValue<MicroService> cloneLineValue = new CloneLineValue<MicroService>(ms);
    		Iterable<Project> projects = containRelationService.findMicroServiceContainProjects(ms);
    		for(Project project : projects) {
        		CloneLineValue<Project> projectResult = projectResults.get(project);
        		if(projectResult == null) {
        			continue;
        		}
        		cloneLineValue.addAllFiles(projectResult.getAllFiles());
        		cloneLineValue.addAllCloneFiles(projectResult.getCloneFiles());
        		cloneLineValue.addAllCloneFunctions(projectResult.getCloneFunctions());
        	}
    		result.put(ms, cloneLineValue);
    	}
    	return result;
    }
    
    private Map<Boolean, Map<Integer, Map<Long, CloneLineValue<MicroService>>>> msCloneLineValuesCalculateGroupByFunctionCache = new ConcurrentHashMap<>();
    @Override
    public Map<Integer, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFunction(
    		Collection<MicroService> mss, boolean removeFileLevelClone) {
    	if(msCloneLineValuesCalculateGroupByFunctionCache.get(removeFileLevelClone) != null) {
    		return msCloneLineValuesCalculateGroupByFunctionCache.get(removeFileLevelClone);
    	}
    	Map<Integer, Map<Long, CloneLineValue<MicroService>>> result = new HashMap<>();
    	Map<Integer, Map<Project, CloneLineValue<Project>>> projectGroups = 
    			projectCloneLineValuesCalculateGroupByFunction(removeFileLevelClone);
    	LOGGER.info("finish projectCloneLineValuesCalculateGroupByFunction");
    	for(Map.Entry<Integer, Map<Project, CloneLineValue<Project>>> projectGroup : projectGroups.entrySet()) {
    		int group = projectGroup.getKey();
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
    		result.put(group, mssValue);
    	}
    	msCloneLineValuesCalculateGroupByFunctionCache.put(removeFileLevelClone, result);
    	return result;
    }
    
    Map<Boolean, Map<Integer, Map<Long, CloneLineValue<MicroService>>>> msCloneLineValuesCalculateGroupByFileCache = new ConcurrentHashMap<>();
    @Override
    public Map<Integer, Map<Long, CloneLineValue<MicroService>>> msCloneLineValuesCalculateGroupByFile(Collection<MicroService> mss, boolean removeDataClass) {
    	if(msCloneLineValuesCalculateGroupByFileCache.get(removeDataClass) != null) {
    		return msCloneLineValuesCalculateGroupByFileCache.get(removeDataClass);
    	}
    	Map<Integer, Map<Long, CloneLineValue<MicroService>>> result = new HashMap<>();
    	Map<Integer, Map<Project, CloneLineValue<Project>>> projectGroups = projectCloneLineValuesCalculateGroupByFile(removeDataClass);
    	LOGGER.info("finish projectCloneLineValuesCalculateGroupByFile");
    	for(Map.Entry<Integer, Map<Project, CloneLineValue<Project>>> projectGroup : projectGroups.entrySet()) {
    		int group = projectGroup.getKey();
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
    		result.put(group, mssValue);
    	}
    	msCloneLineValuesCalculateGroupByFileCache.put(removeDataClass, result);
    	return result;
    }

    @Override
    public CloneLineValue<MicroService> msCloneLineValuesGroup(MicroService ms, int group, CloneLevel level,
    		boolean removeFileLevelClone, boolean removeDataClass) {
    	CloneLineValue<MicroService> result = new CloneLineValue<>(ms);
    	Map<Integer, Map<Project, CloneLineValue<Project>>> projectResultsGroup = null;
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
    
    Map<Boolean, Map<Integer, Map<Project, CloneLineValue<Project>>>> projectCloneValuesCalculateByFileCache = new ConcurrentHashMap<>();
    @Override
    public Map<Integer, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFile(boolean removeDataClass) {
    	if(projectCloneValuesCalculateByFileCache.get(removeDataClass) != null) {
    		return projectCloneValuesCalculateByFileCache.get(removeDataClass);
    	}
    	Collection<Collection<? extends Node>> groupFileClones = groupFileCloneNode(removeDataClass);
    	LOGGER.info("finish groupFileCloneNode");
    	Map<Integer, Map<Project, CloneLineValue<Project>>> result = new HashMap<>();
    	for(int i = -1; i < groupFileClones.size(); i++) {
    		Map<Project, CloneLineValue<Project>> projectToCloneValue = new HashMap<>();
    		for(Project project : staticAnalyseService.allProjects()) {
    			CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
    			projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
    			projectToCloneValue.put(project, projectCloneValue);
    		}
    		result.put(i, projectToCloneValue);
    	}
    	int index = 0;
    	for(Collection<? extends Node> group : groupFileClones) {
    		for(Node node : group) {
    			ProjectFile file = (ProjectFile) node;
    			if(removeDataClass && staticAnalyseService.isDataFile(file)) {
    				continue;
    			}
    			Project project = containRelationService.findFileBelongToProject(file);
    			CloneLineValue<Project> allProjectCloneValue = result.get(-1).get(project);
    			CloneLineValue<Project> groupProjectCloneValue = result.get(index).get(project);
    			allProjectCloneValue.addCloneFile(file);
    			groupProjectCloneValue.addCloneFile(file);
    			result.get(index).put(project, groupProjectCloneValue);
    			result.get(-1).put(project, allProjectCloneValue);
    		}
    		index++;
    	}
    	projectCloneValuesCalculateByFileCache.put(removeDataClass, result);
    	return result;
    }
    
    Map<Project, CloneLineValue<Project>> projectCloneValuesCache = null;
    @Override
    public Map<Project, CloneLineValue<Project>> projectCloneLineValues() {
    	if(projectCloneValuesCache != null) {
    		return projectCloneValuesCache;
    	}
    	Map<Project, CloneLineValue<Project>> result = new HashMap<>();
    	for(Project project : staticAnalyseService.allProjects()) {
    		CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
    		projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
    		result.put(project, projectCloneValue);
    	}
    	for(FunctionCloneFunction cloneRelation : findAllFunctionCloneFunctions()) {
    		result.get(containRelationService.findFunctionBelongToProject(cloneRelation.getFunction1())).addCloneFunction(cloneRelation.getFunction1());
    		result.get(containRelationService.findFunctionBelongToProject(cloneRelation.getFunction2())).addCloneFunction(cloneRelation.getFunction2());
    	}
    	for(FileCloneFile cloneRelation : findAllFileCloneFiles()) {
    		result.get(containRelationService.findFileBelongToProject(cloneRelation.getFile1())).addCloneFile(cloneRelation.getFile1());
    		result.get(containRelationService.findFileBelongToProject(cloneRelation.getFile2())).addCloneFile(cloneRelation.getFile2());
    	}
    	projectCloneValuesCache = result;
    	return result;
    }
    
    Map<Boolean, Map<Integer, Map<Project, CloneLineValue<Project>>>> projectCloneValuesCalculateByFunctionCache = new ConcurrentHashMap<>();
    @Override
    public Map<Integer, Map<Project, CloneLineValue<Project>>> projectCloneLineValuesCalculateGroupByFunction(
    		boolean removeFileLevelClone) {
    	if(projectCloneValuesCalculateByFunctionCache.get(removeFileLevelClone) != null) {
    		return projectCloneValuesCalculateByFunctionCache.get(removeFileLevelClone);
    	}
//    	Collection<Collection<? extends Node>> groupFunctionClones = groupFunctionCloneNode();
    	Collection<Collection<? extends CloneRelation>> groupFunctionCloneRelations = groupFunctionCloneRelation(removeFileLevelClone);
    	Map<Integer, Map<Project, CloneLineValue<Project>>> result = new HashMap<>();
    	for(int i = -1; i < groupFunctionCloneRelations.size(); i++) {
    		Map<Project, CloneLineValue<Project>> projectToCloneValue = new HashMap<>();
    		for(Project project : staticAnalyseService.allProjects()) {
    			CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
    			projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
    			projectToCloneValue.put(project, projectCloneValue);
    		}
    		result.put(i, projectToCloneValue);
    	}
    	int index = 0;
    	for(Collection<? extends CloneRelation> group : groupFunctionCloneRelations) {
    		for(CloneRelation relation : group) {
    			FunctionCloneFunction clone = (FunctionCloneFunction) relation;
    			Function function1 = clone.getFunction1();
    			Function function2 = clone.getFunction2();
    			ProjectFile file1 = containRelationService.findFunctionBelongToFile(function1);
    			ProjectFile file2 = containRelationService.findFunctionBelongToFile(function2);
    			if(removeFileLevelClone && isCloneBetweenFiles(file1, file2)) {
    				continue;
    			}
    			Project project1 = containRelationService.findFunctionBelongToProject(function1);
    			Project project2 = containRelationService.findFunctionBelongToProject(function2);
    			CloneLineValue<Project> allProjectCloneValue1 = result.get(-1).get(project1);
    			CloneLineValue<Project> groupProjectCloneValue1 = result.get(index).get(project1);
    			CloneLineValue<Project> allProjectCloneValue2 = result.get(-1).get(project2);
    			CloneLineValue<Project> groupProjectCloneValue2 = result.get(index).get(project2);
    			allProjectCloneValue1.addCloneFunction(function1);
    			groupProjectCloneValue1.addCloneFunction(function1);
    			result.get(index).put(project1, groupProjectCloneValue1);
    			result.get(-1).put(project1, allProjectCloneValue1);
    			allProjectCloneValue2.addCloneFunction(function2);
    			groupProjectCloneValue2.addCloneFunction(function2);
    			result.get(index).put(project2, groupProjectCloneValue2);
    			result.get(-1).put(project2, allProjectCloneValue2);
    		}
    		index++;
    	}
    	projectCloneValuesCalculateByFunctionCache.put(removeFileLevelClone, result);
    	return result;
    }
    
    public CloneLineValue<Project> projectCloneValuesCalculate(Project project, int groupIndex, 
    		Class<? extends CloneRelationNode> nodeClass,
    		boolean removeFileLevelClone, boolean removeDataClass) {
    	CloneLineValue<Project> projectCloneValue = new CloneLineValue<Project>(project);
    	projectCloneValue.addAllFiles(containRelationService.findProjectContainAllFiles(project));
    	int index = 0;
    	if(nodeClass == Function.class) {
    		for(Collection<? extends Node> group : groupFunctionCloneNode(removeFileLevelClone)) {
    			if(groupIndex == index || groupIndex < 0) {
    				for(Node node : group) {
    					Function function = (Function) node;
    					if(containRelationService.findFunctionBelongToProject(function).equals(project)) {
    						projectCloneValue.addCloneFunction(function);
    					}
    				}
    			}
    			index++;
    		}
    	} else {
    		for(Collection<? extends Node> group : groupFileCloneNode(removeDataClass)) {
    			if(groupIndex == index || groupIndex < 0) {
    				for(Node node : group) {
    					ProjectFile file = (ProjectFile) node;
    					if(containRelationService.findFileBelongToProject(file).equals(project)) {
    						projectCloneValue.addCloneFile(file);
    					}
    				}
    			}
    			index++;
    		}
    	}
    	return projectCloneValue;
    }
    
	Map<Boolean, Collection<Collection<? extends CloneRelation>>> groupFunctionCloneRelationCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Collection<? extends CloneRelation>> groupFunctionCloneRelation(boolean removeFileClone) {
		Collection<Collection<? extends CloneRelation>> result = groupFunctionCloneRelationCache.get(removeFileClone);
		if(result == null) {
			result = groupCloneRelations(findAllFunctionCloneFunctions(), removeFileClone, false);
			groupFunctionCloneRelationCache.put(removeFileClone, result);
		}
		return result;
	}

	Map<Boolean, Collection<Collection<? extends CloneRelation>>> groupFileCloneRelationCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Collection<? extends CloneRelation>> groupFileCloneRelation(boolean removeDataClass) {
		Collection<Collection<? extends CloneRelation>> result = groupFileCloneRelationCache.get(removeDataClass);
		if(result == null) {
			result = groupCloneRelations(findAllFileCloneFiles(), false, removeDataClass);
			groupFileCloneRelationCache.put(removeDataClass, result);
		}
		return result;
	}
	
	Map<Boolean, Collection<Collection<? extends Node>>> groupFunctionCloneNodeCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Collection<? extends Node>> groupFunctionCloneNode(boolean removeFileClone) {
		Collection<Collection<? extends Node>> result = groupFunctionCloneNodeCache.get(removeFileClone);
		if(result == null) {
			result = groupCloneNodes(findAllFunctionCloneFunctions(), removeFileClone, false);
			groupFunctionCloneNodeCache.put(removeFileClone, result);
		}
		return result;
	}

	Map<Boolean, Collection<Collection<? extends Node>>> groupFileCloneNodeCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Collection<? extends Node>> groupFileCloneNode(boolean removeDataClass) {
		Collection<Collection<? extends Node>> result = groupFileCloneNodeCache.get(removeDataClass);
		if(result == null) {
			result = groupCloneNodes(findAllFileCloneFiles(), false, removeDataClass);
			groupFileCloneNodeCache.put(removeDataClass, result);
		}
		LOGGER.info("finish groupCloneNodes");
		return result;
	}
	
	private Collection<Collection<? extends Node>> groupCloneNodes(
			Iterable<? extends CloneRelation> relations, 
			boolean removeFileClone,
			boolean removeDataClass) {
		List<Collection<? extends Node>> result = new ArrayList<>();
		Map<Node, Collection<Node>> nodeToCollection = new HashMap<>();
		for(CloneRelation relation : relations) {
			LOGGER.info(new StringBuilder().append("relation: ")
					.append(relation.getStartNodeGraphId()).append(" ")
					.append(relation.getEndNodeGraphId()).toString());
			Node node1 = relation.getStartNode();
			Node node2 = relation.getEndNode();
			if(removeDataClass && node1 instanceof ProjectFile) {
				ProjectFile file1 = (ProjectFile) node1;
				ProjectFile file2 = (ProjectFile) node2;
				if(staticAnalyseService.isDataFile(file1) || staticAnalyseService.isDataFile(file2)) {
					continue;
				}
			} 
			if(removeFileClone && node2 instanceof Function) {
				Function function1 = (Function) node1;
				Function function2 = (Function) node2;
				ProjectFile file1 = containRelationService.findFunctionBelongToFile(function1);
				ProjectFile file2 = containRelationService.findFunctionBelongToFile(function2);
				if(isCloneBetweenFiles(file1, file2)) {
					continue;
				}
			}
			Collection<Node> collections1 = nodeToCollection.get(node1);
			Collection<Node> collections2 = nodeToCollection.get(node2);
			if(collections1 == null && collections2 == null) {
				collections1 = new ArrayList<>();
				collections1.add(node1);
				collections1.add(node2);
				result.add(collections1);
				nodeToCollection.put(node1, collections1);
				nodeToCollection.put(node2, collections1);
			} else if(collections1 != null && collections2 == null) {
				collections1.add(node2);
				nodeToCollection.put(node2, collections1);
			} else if(collections1 == null && collections2 != null) {
				collections2.add(node1);
				nodeToCollection.put(node1, collections2);
			} else {
				if(collections1 != collections2) {
					collections1.addAll(collections2);
					result.remove(collections2);
					for(Node node : collections2) {
						nodeToCollection.put(node, collections1);
					}
				}
			}
		}
		result.sort((collection1, collection2) -> {
			return collection2.size() - collection1.size();
		});
		return result;
	}
	
	private Collection<Collection<? extends CloneRelation>> groupCloneRelations(
			Iterable<? extends CloneRelation> relations, 
			boolean removeFileClone,
			boolean removeDataClass) {
		List<Collection<? extends CloneRelation>> result = new ArrayList<>();
		Map<Node, Collection<CloneRelation>> nodeToCollection = new HashMap<>();
		for(CloneRelation relation : relations) {
			Node node1 = relation.getStartNode();
			Node node2 = relation.getEndNode();
			if(removeDataClass && node1 instanceof ProjectFile) {
				ProjectFile file1 = (ProjectFile) node1;
				ProjectFile file2 = (ProjectFile) node2;
				if(staticAnalyseService.isDataFile(file1) || staticAnalyseService.isDataFile(file2)) {
					continue;
				}
			} 
			if(removeFileClone && node2 instanceof Function) {
				Function function1 = (Function) node1;
				Function function2 = (Function) node2;
				ProjectFile file1 = containRelationService.findFunctionBelongToFile(function1);
				ProjectFile file2 = containRelationService.findFunctionBelongToFile(function2);
				if(isCloneBetweenFiles(file1, file2)) {
					continue;
				}
			}
			Collection<CloneRelation> collections1 = nodeToCollection.get(node1);
			Collection<CloneRelation> collections2 = nodeToCollection.get(node2);
			if(collections1 == null && collections2 == null) {
				collections1 = new ArrayList<>();
				collections1.add(relation);
				result.add(collections1);
				nodeToCollection.put(node1, collections1);
				nodeToCollection.put(node2, collections1);
			} else if(collections1 != null && collections2 == null) {
				collections1.add(relation);
				nodeToCollection.put(node2, collections1);
			} else if(collections1 == null && collections2 != null) {
				collections2.add(relation);
				nodeToCollection.put(node1, collections2);
			} else {
				if(collections1 != collections2) {
					collections1.addAll(collections2);
					result.remove(collections2);
					for(CloneRelation r : collections2) {
						nodeToCollection.put(r.getStartNode(), collections1);
						nodeToCollection.put(r.getEndNode(), collections1);
					}
				}
				collections1.add(relation);
			}
		}
		result.sort((collection1, collection2) -> {
			return collection2.size() - collection1.size();
		});
		return result;
	}

	/*@Override
	public Collection<Collection<Clone<Function, FunctionCloneFunction>>> queryFunctionCloneGroup() {
		List<Collection<Clone<Function, FunctionCloneFunction>>> result = new ArrayList<>();
		return result;
	}

	@Override
	public Collection<Collection<Clone<ProjectFile, FileCloneFile>>> queryFileCloneGroup() {
		List<Collection<Clone<ProjectFile, FileCloneFile>>> result = new ArrayList<>();
		return result;
	}*/

	@Override
	public Iterable<FileCloneFile> queryProjectContainFileCloneFiles(Project project) {
		Iterable<FileCloneFile> allClones = findAllFileCloneFiles();
		List<FileCloneFile> result = new ArrayList<>();
		for(FileCloneFile clone : allClones) {
			if(containRelationService.findFileBelongToProject(clone.getFile1()).equals(project)
					&& containRelationService.findFileBelongToProject(clone.getFile2()).equals(project)) {
				result.add(clone);
			}
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
	
	private Iterable<FunctionCloneFunction> allFunctionClonesCache = null;
	@Override
	public Iterable<FunctionCloneFunction> findAllFunctionCloneFunctions() {
		if(allFunctionClonesCache == null) {
			allFunctionClonesCache = functionCloneFunctionRepository.findAll();
		}
		return allFunctionClonesCache;
	}
	
	@Override
	public Iterable<FunctionCloneFunction> findProjectContainFunctionCloneFunctions(Project project) {
		Iterable<FunctionCloneFunction> allClones = findAllFunctionCloneFunctions();
		List<FunctionCloneFunction> result = new ArrayList<>();
		for(FunctionCloneFunction clone : allClones) {
			if(containRelationService.findFunctionBelongToProject(clone.getFunction1()).equals(project)
					&& containRelationService.findFunctionBelongToProject(clone.getFunction2()).equals(project)) {
				result.add(clone);
			}
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
		Map<Integer, Map<Long, CloneLineValue<MicroService>>> temp = new HashMap<>();
		Map<MicroService, Integer> msCount = new HashMap<>();
		if(level == CloneLevel.function) {
			temp = msCloneLineValuesCalculateGroupByFunction(mss, removeFileLevelClone);
		} else {
			temp = msCloneLineValuesCalculateGroupByFile(mss, removeDataClass);
		}
		for(int group : temp.keySet()) {
			if(group == -1) {
				continue;
			}
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

	private Iterable<FileCloneFile> allFileClonesCache = null;
	@Override
	public Iterable<FileCloneFile> findAllFileCloneFiles() {
		if(allFileClonesCache == null) {
			allFileClonesCache = fileCloneFileRepository.findAll();
			for(FileCloneFile clone : allFileClonesCache) {
				ProjectFile file1 = clone.getFile1();
				ProjectFile file2 = clone.getFile2();
				Map<ProjectFile, FileCloneFile> file1ToFileClones = fileToFileClones.getOrDefault(file1, new ConcurrentHashMap<>());
				Map<ProjectFile, FileCloneFile> file2ToFileClones = fileToFileClones.getOrDefault(file2, new ConcurrentHashMap<>());
				file1ToFileClones.put(file2, clone);
				file2ToFileClones.put(file1, clone);
				fileToFileClones.put(file1, file1ToFileClones);
				fileToFileClones.put(file2, file2ToFileClones);
			}
		}
		return allFileClonesCache;
	}	

	Map<ProjectFile, Map<ProjectFile, FileCloneFile>> fileToFileClones = new ConcurrentHashMap<>();
	@Override
	public boolean isCloneBetweenFiles(ProjectFile file1, ProjectFile file2) {
		Map<ProjectFile, FileCloneFile> file1ToFileClones = fileToFileClones.getOrDefault(file1, new ConcurrentHashMap<>());
		Map<ProjectFile, FileCloneFile> file2ToFileClones = fileToFileClones.getOrDefault(file2, new ConcurrentHashMap<>());
		if(file1ToFileClones.get(file2) != null) {
			System.out.println("isCloneBetweenFiles true");
			return true;
		}
		if(file2ToFileClones.get(file1) != null) {
			System.out.println("isCloneBetweenFiles true");
			return true;
		}
		System.out.println("isCloneBetweenFiles false");
		return false;
	}
	
}
