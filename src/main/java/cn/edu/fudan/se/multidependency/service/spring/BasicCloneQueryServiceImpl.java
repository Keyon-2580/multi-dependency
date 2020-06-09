package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.repository.node.clone.CloneGroupRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FileCloneFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FunctionCloneFunctionRepository;

@Service
public class BasicCloneQueryServiceImpl implements BasicCloneQueryService {

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
    private CloneGroupRepository cloneGroupRepository;
    
	private Map<CloneGroup, Collection<FunctionCloneFunction>> groupContainFunctionCloneRelationsCache = new ConcurrentHashMap<>();
	@Override
	public Collection<FunctionCloneFunction> queryGroupContainFunctionClones(CloneGroup group) {
		Collection<FunctionCloneFunction> result = groupContainFunctionCloneRelationsCache.get(group);
		if(result == null) {
			result = functionCloneFunctionRepository.findCloneGroupContainFunctionClones(group.getId());
		}
		groupContainFunctionCloneRelationsCache.put(group, result);
		return result;
	}
	
	private Map<CloneGroup, Collection<FileCloneFile>> groupContainFileCloneRelationsCache = new ConcurrentHashMap<>();
	@Override
	public Collection<FileCloneFile> queryGroupContainFileClones(CloneGroup group) {
		Collection<FileCloneFile> result = groupContainFileCloneRelationsCache.get(group);
		if(result == null) {
			result = fileCloneFileRepository.findCloneGroupContainFileClones(group.getId());
		}
		groupContainFileCloneRelationsCache.put(group, result);
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

	private Iterable<FileCloneFile> allFileClonesCache = null;
	@Override
	public Iterable<FileCloneFile> findAllFileCloneFiles() {
		if(allFileClonesCache == null) {
			allFileClonesCache = fileCloneFileRepository.findAll();
		}
		return allFileClonesCache;
	}	
	
	@Override
	public Iterable<FunctionCloneFunction> queryProjectContainFunctionCloneFunctions(Project project) {
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
	
    private Map<CloneLevel, Collection<CloneGroup>> cloneLevelToGroupsCache = new ConcurrentHashMap<>();
    @Override
	public Collection<CloneGroup> queryGroups(CloneLevel level) {
    	if(cloneLevelToGroupsCache.get(level) != null) {
    		return cloneLevelToGroupsCache.get(level);
    	}
    	String cloneLevel = "";
    	if(level == CloneLevel.function) {
    		cloneLevel = NodeLabelType.Function.toString();
    	} else {
    		cloneLevel = NodeLabelType.ProjectFile.toString();
    	}
		List<CloneGroup> result = cloneGroupRepository.findCloneGroupsByLevel(cloneLevel);
		result.sort((group1, group2) -> {
			return group2.getSize() - group1.getSize();
		});
		cloneLevelToGroupsCache.put(level, result);
		for(CloneGroup group : result) {
			cacheService.cacheNodeById(group);
		}
		return result;
	}
}
