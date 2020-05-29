package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FileCloneFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FunctionCloneFunctionRepository;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;

@Service
public class CloneAnalyseServiceImpl implements CloneAnalyseService {
    
    @Autowired
    FunctionCloneFunctionRepository functionCloneFunctionRepository;
    
    @Autowired
    FileCloneFileRepository fileCloneFileRepository;
    
    @Autowired
    ContainRelationService containRelationService;
    
    @Autowired
    CacheService cache;

	@Override
	public Collection<Collection<? extends CloneRelation>> groupFunctionCloneRelation() {
		return groupCloneRelations(findAllFunctionCloneFunctions());
	}

	@Override
	public Collection<Collection<? extends CloneRelation>> groupFileCloneRelation() {
		return groupCloneRelations(findAllFileCloneFiles());
	}
	
	@Override
	public Collection<Collection<? extends Node>> groupFunctionCloneNode() {
		return groupCloneNodes(findAllFunctionCloneFunctions());
	}

	@Override
	public Collection<Collection<? extends Node>> groupFileCloneNode() {
		return groupCloneNodes(findAllFileCloneFiles());
	}
	
	private Collection<Collection<? extends Node>> groupCloneNodes(Iterable<? extends CloneRelation> relations) {
		List<Collection<? extends Node>> result = new ArrayList<>();
		Map<Node, Collection<Node>> nodeToCollection = new HashMap<>();
		for(CloneRelation relation : relations) {
			Node node1 = relation.getStartNode();
			Node node2 = relation.getEndNode();
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
	
	private Collection<Collection<? extends CloneRelation>> groupCloneRelations(Iterable<? extends CloneRelation> relations) {
		List<Collection<? extends CloneRelation>> result = new ArrayList<>();
		Map<Node, Collection<CloneRelation>> nodeToCollection = new HashMap<>();
		for(CloneRelation relation : relations) {
			Node node1 = relation.getStartNode();
			Node node2 = relation.getEndNode();
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

	@Override
	public Collection<Collection<Clone<Function, FunctionCloneFunction>>> queryFunctionCloneGroup() {
		List<Collection<Clone<Function, FunctionCloneFunction>>> result = new ArrayList<>();
		return result;
	}

	@Override
	public Collection<Collection<Clone<ProjectFile, FileCloneFile>>> queryFileCloneGroup() {
		List<Collection<Clone<ProjectFile, FileCloneFile>>> result = new ArrayList<>();
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
	
	private Iterable<FileCloneFile> allFileClonesCache = null;
	@Override
	public Iterable<FileCloneFile> findAllFileCloneFiles() {
		if(allFileClonesCache == null) {
			allFileClonesCache = fileCloneFileRepository.findAll();
		}
		return allFileClonesCache;
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
	
}
