package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FileCloneFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.FunctionCloneFunctionRepository;
import cn.edu.fudan.se.multidependency.service.spring.data.Clone;
import cn.edu.fudan.se.multidependency.service.spring.data.CloneValueCalculatorForMicroService;
import cn.edu.fudan.se.multidependency.utils.CytoscapeUtil;

@Service
public class CloneAnalyseServiceImpl implements CloneAnalyseService {
    
    @Autowired
    FunctionCloneFunctionRepository functionCloneFunctionRepository;
    
    @Autowired
    FileCloneFileRepository fileCloneFileRepository;
    
    @Autowired
    ContainRelationService containRelationService;
    
    @Autowired
    CacheService cacheService;
	
//	@Autowired
//	private MicroserviceService msService;
	
    @Override
	public JSONObject fileCloneFilesToCytoscape(Collection<FileCloneFile> groupRelations) {
		JSONObject result = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray edges = new JSONArray();
		Map<Node, Boolean> isNodeToCytoscapeNode = new HashMap<>();
		Map<String, Boolean> isIdToCytoscapeEdge = new HashMap<>();
		for(FileCloneFile cloneRelation : groupRelations) {
			ProjectFile file1 = cloneRelation.getFile1();
			if(!isNodeToCytoscapeNode.getOrDefault(file1, false)) {
				isNodeToCytoscapeNode.put(file1, true);
				nodes.add(CytoscapeUtil.toCytoscapeNode(file1, "File"));
			}
			ProjectFile file2 = cloneRelation.getFile2();
			if(!isNodeToCytoscapeNode.getOrDefault(file2, false)) {
				isNodeToCytoscapeNode.put(file2, true);
				nodes.add(CytoscapeUtil.toCytoscapeNode(file2, "File"));
			}
			edges.add(CytoscapeUtil.relationToEdge(file1, file2, "FileCloneFile", String.valueOf(cloneRelation.getValue()), true));
			Project project1 = containRelationService.findFileBelongToProject(file1);
			if(!isNodeToCytoscapeNode.getOrDefault(project1, false)) {
				isNodeToCytoscapeNode.put(project1, true);
				nodes.add(CytoscapeUtil.toCytoscapeNode(project1, project1.getName() + "(" + project1.getLanguage().toString() + ")", "Project"));
			}
			String project1ContainFile1Id = String.join("_", String.valueOf(project1.getId()), String.valueOf(file1.getId()));
			if(!isIdToCytoscapeEdge.getOrDefault(project1ContainFile1Id, false)) {
				isIdToCytoscapeEdge.put(project1ContainFile1Id, true);
				edges.add(CytoscapeUtil.relationToEdge(project1, file1, "Contain", "", true));
			}
			Project project2 = containRelationService.findFileBelongToProject(file2);
			if(!isNodeToCytoscapeNode.getOrDefault(project2, false)) {
				isNodeToCytoscapeNode.put(project2, true);
				nodes.add(CytoscapeUtil.toCytoscapeNode(project2, project2.getName() + "(" + project2.getLanguage().toString() + ")", "Project"));
			}
			String project2ContainFile2Id = String.join("_", String.valueOf(project2.getId()), String.valueOf(file2.getId()));
			if(!isIdToCytoscapeEdge.getOrDefault(project2ContainFile2Id, false)) {
				isIdToCytoscapeEdge.put(project2ContainFile2Id, true);
				edges.add(CytoscapeUtil.relationToEdge(project2, file2, "Contain", "", true));
			}
			/*MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
			if(ms1 != null) {
				if(!isNodeToCytoscapeNode.getOrDefault(ms1, false)) {
					isNodeToCytoscapeNode.put(ms1, true);
					nodes.add(CytoscapeUtil.toCytoscapeNode(ms1, "MicroService"));
				}
				String ms1ContainProject1Id = String.join("_", String.valueOf(ms1.getId()), String.valueOf(project1.getId()));
				if(!isIdToCytoscapeEdge.getOrDefault(ms1ContainProject1Id, false)) {
					isIdToCytoscapeEdge.put(ms1ContainProject1Id, true);
					edges.add(CytoscapeUtil.relationToEdge(ms1, project1, "Contain", "", true));
				}
			}
			MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
			if(ms2 != null) {
				if(!isNodeToCytoscapeNode.getOrDefault(ms2, false)) {
					isNodeToCytoscapeNode.put(ms2, true);
					nodes.add(CytoscapeUtil.toCytoscapeNode(ms2, "MicroService"));
				}
				String ms2ContainProject2Id = String.join("_", String.valueOf(ms2.getId()), String.valueOf(project2.getId()));
				if(!isIdToCytoscapeEdge.getOrDefault(ms2ContainProject2Id, false)) {
					isIdToCytoscapeEdge.put(ms2ContainProject2Id, true);
					edges.add(CytoscapeUtil.relationToEdge(ms2, project2, "Contain", "", true));
				}
			}*/
		}
		
		result.put("nodes", nodes);
		result.put("edges", edges);
		return result;
	}
    
	Collection<Collection<? extends CloneRelation>> groupFunctionCloneRelationCache = null;
	@Override
	public Collection<Collection<? extends CloneRelation>> groupFunctionCloneRelation() {
		return groupFunctionCloneRelationCache == null ? (groupFunctionCloneRelationCache = groupCloneRelations(findAllFunctionCloneFunctions())) : groupFunctionCloneRelationCache;
	}

	Collection<Collection<? extends CloneRelation>> groupFileCloneRelationCache = null;
	@Override
	public Collection<Collection<? extends CloneRelation>> groupFileCloneRelation() {
		return groupFileCloneRelationCache == null ? (groupFileCloneRelationCache = groupCloneRelations(findAllFileCloneFiles())) : groupFileCloneRelationCache;
	}
	
	Collection<Collection<? extends Node>> groupFunctionCloneNodeCache = null;
	@Override
	public Collection<Collection<? extends Node>> groupFunctionCloneNode() {
		return groupFunctionCloneNodeCache == null ? (groupFunctionCloneNodeCache = groupCloneNodes(findAllFunctionCloneFunctions())) : groupFunctionCloneNodeCache;
	}

	Collection<Collection<? extends Node>> groupFileCloneNodeCache = null;
	@Override
	public Collection<Collection<? extends Node>> groupFileCloneNode() {
		return groupFileCloneNodeCache == null ? (groupFileCloneNodeCache = groupCloneNodes(findAllFileCloneFiles())) : groupFileCloneNodeCache;
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
	
}
