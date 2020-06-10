package cn.edu.fudan.se.multidependency.service.spring.show;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.FileCloneFile;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import cn.edu.fudan.se.multidependency.service.spring.CloneAnalyseService;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.data.FileCloneGroup;
import cn.edu.fudan.se.multidependency.service.spring.data.FunctionCloneGroup;
import cn.edu.fudan.se.multidependency.utils.CytoscapeUtil;
import cn.edu.fudan.se.multidependency.utils.CytoscapeUtil.CytoscapeEdge;
import cn.edu.fudan.se.multidependency.utils.CytoscapeUtil.CytoscapeNode;
import cn.edu.fudan.se.multidependency.utils.ZTreeUtil.ZTreeNode;

@Service
public class CloneShowServiceImpl implements CloneShowService {
	
	@Autowired
	private CloneAnalyseService cloneAnalyseService;
	
	@Autowired
	private ContainRelationService containRelationService;
	
	private JSONObject fileClonesGroupsToCytoscape(Collection<CloneGroup> groups, 
			boolean showGroupNode, boolean removeDataClass) {
		Collection<FileCloneGroup> fileGroups = cloneAnalyseService.groupFileClones(removeDataClass);
		Map<Node, ZTreeNode> nodeToZTreeNode = new HashMap<>();
		Map<Project, ZTreeNode> projectToZTreeNode = new HashMap<>();
		Map<MicroService, ZTreeNode> msToZTreeNode = new HashMap<>();
		Set<ZTreeNode> groupZTreeNodes = new HashSet<>();
		JSONObject result = new JSONObject();
		List<CytoscapeNode> nodes = new ArrayList<>();
		List<CytoscapeEdge> edges = new ArrayList<>();
		List<CytoscapeNode> groupNodes = new ArrayList<>();
		List<CytoscapeEdge> groupEdges = new ArrayList<>();
		Map<Node, Boolean> isNodeToCytoscapeNode = new HashMap<>();
		Map<String, Boolean> isIdToCytoscapeEdge = new HashMap<>();
		for(FileCloneGroup fileGroup : fileGroups) {
			if(!groups.contains(fileGroup.getGroup())) {
				continue;
			}
			CloneGroup cloneGroup = fileGroup.getGroup();
			CytoscapeNode groupNode = new CytoscapeNode(cloneGroup.getId(), cloneGroup.getName(), "CloneGroup");
			groupNodes.add(groupNode);
			groupZTreeNodes.add(new ZTreeNode(cloneGroup.getId(), cloneGroup.getName(), false, "CloneGroup", false));
			
			for(FileCloneFile cloneRelation : fileGroup.getRelations()) {
				ProjectFile file1 = cloneRelation.getFile1();
				ProjectFile file2 = cloneRelation.getFile2();
				if(!isNodeToCytoscapeNode.getOrDefault(file1, false)) {
					isNodeToCytoscapeNode.put(file1, true);
					CytoscapeNode file1CytoscapeNode = new CytoscapeNode(file1.getId(), file1.getName() + "\n(" + file1.getLine() + ")", "File");
					file1CytoscapeNode.setValue(file1.getPath());
					nodes.add(file1CytoscapeNode);
					groupEdges.add(new CytoscapeEdge(file1.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
					nodeToZTreeNode.put(file1, new ZTreeNode(file1.getId(), file1.getPath() + "(" + file1.getLine() + ")", false, "File", false));
				}
				if(!isNodeToCytoscapeNode.getOrDefault(file2, false)) {
					isNodeToCytoscapeNode.put(file2, true);
					CytoscapeNode file2CytoscapeNode = new CytoscapeNode(file2.getId(), file2.getName() + "\n(" + file2.getLine() + ")", "File");
					file2CytoscapeNode.setValue(file2.getPath());
					nodes.add(file2CytoscapeNode);
					groupEdges.add(new CytoscapeEdge(file2.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
					nodeToZTreeNode.put(file2, new ZTreeNode(file2.getId(), file2.getPath() + "(" + file2.getLine() + ")", false, "File", false));
				}
				edges.add(new CytoscapeEdge(file1, file2, "Clone", String.valueOf(cloneRelation.getValue())));
				Project project1 = containRelationService.findFileBelongToProject(file1);
				MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
				if(ms1 == null) {
					if(!isNodeToCytoscapeNode.getOrDefault(project1, false)) {
						isNodeToCytoscapeNode.put(project1, true);
						nodes.add(new CytoscapeNode(project1.getId(), project1.getName() + "(" + project1.getLanguage().toString() + ")", "Project"));
						projectToZTreeNode.put(project1, new ZTreeNode(project1.getId(), project1.getName() + "(" + project1.getLanguage() + ")", false, "Project", true));
					}
					String project1ContainFile1Id = String.join("_", String.valueOf(project1.getId()), String.valueOf(file1.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(project1ContainFile1Id, false)) {
						isIdToCytoscapeEdge.put(project1ContainFile1Id, true);
						edges.add(new CytoscapeEdge(project1, file1, "Contain"));
						projectToZTreeNode.get(project1).addChild(nodeToZTreeNode.get(file1));
					}
				} else {
					if(!isNodeToCytoscapeNode.getOrDefault(ms1, false)) {
						isNodeToCytoscapeNode.put(ms1, true);
						nodes.add(new CytoscapeNode(ms1.getId(), ms1.getName(), "MicroService"));
						msToZTreeNode.put(ms1, new ZTreeNode(ms1.getId(), ms1.getName(), false, "MicroService", true));
					}
					String ms1ContainFile1Id = String.join("_", String.valueOf(ms1.getId()), String.valueOf(file1.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(ms1ContainFile1Id, false)) {
						isIdToCytoscapeEdge.put(ms1ContainFile1Id, true);
						edges.add(new CytoscapeEdge(ms1, file1, "Contain"));
						msToZTreeNode.get(ms1).addChild(nodeToZTreeNode.get(file1));
					}
				}
				Project project2 = containRelationService.findFileBelongToProject(file2);
				MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
				if(ms2 == null) {
					if(!isNodeToCytoscapeNode.getOrDefault(project2, false)) {
						isNodeToCytoscapeNode.put(project2, true);
						nodes.add(new CytoscapeNode(project2.getId(), project2.getName() + "(" + project2.getLanguage().toString() + ")", "Project"));
						projectToZTreeNode.put(project2, new ZTreeNode(project2.getId(), project2.getName() + "(" + project2.getLanguage() + ")", false, "Project", true));
					}
					String project2ContainFile2Id = String.join("_", String.valueOf(project2.getId()), String.valueOf(file2.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(project2ContainFile2Id, false)) {
						isIdToCytoscapeEdge.put(project2ContainFile2Id, true);
						edges.add(new CytoscapeEdge(project2, file2, "Contain"));
						projectToZTreeNode.get(project2).addChild(nodeToZTreeNode.get(file2));
					}
				} else {
					if(!isNodeToCytoscapeNode.getOrDefault(ms2, false)) {
						isNodeToCytoscapeNode.put(ms2, true);
						nodes.add(new CytoscapeNode(ms2.getId(), ms2.getName(), "MicroService"));
						msToZTreeNode.put(ms2, new ZTreeNode(ms2.getId(), ms2.getName(), false, "MicroService", true));
					}
					String ms2ContainFile2Id = String.join("_", String.valueOf(ms2.getId()), String.valueOf(file2.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(ms2ContainFile2Id, false)) {
						isIdToCytoscapeEdge.put(ms2ContainFile2Id, true);
						edges.add(new CytoscapeEdge(ms2, file2, "Contain"));
						msToZTreeNode.get(ms2).addChild(nodeToZTreeNode.get(file2));
					}
				}
			}
		}
		

		JSONArray ztreeResult = new JSONArray();
		for(ZTreeNode node : projectToZTreeNode.values()) {
			ztreeResult.add(node.toJSON());
		}
		for(ZTreeNode node : msToZTreeNode.values()) {
			ztreeResult.add(node.toJSON());
		}
		if(showGroupNode) {
			nodes.addAll(groupNodes);
			edges.addAll(groupEdges);
			ZTreeNode groupAllNodes = new ZTreeNode(-1, "所有组", true, "NodeGroup", true);
			for(ZTreeNode node : groupZTreeNodes) {
				groupAllNodes.addChild(node);
			}
			ztreeResult.add(groupAllNodes);
		}
		result.put("ztree", ztreeResult);
		result.put("nodes", CytoscapeUtil.toNodes(nodes));
		result.put("edges", CytoscapeUtil.toEdges(edges));
		return result;
	}
	
	private JSONObject functionClonesGroupsToCytoscape(Collection<CloneGroup> groups, 
			boolean showGroupNode, boolean removeFileLevelClone) {
		Collection<FunctionCloneGroup> functionGroups = cloneAnalyseService.groupFunctionClones(removeFileLevelClone);
		Map<Node, ZTreeNode> nodeToZTreeNode = new HashMap<>();
		Map<Project, ZTreeNode> projectToZTreeNode = new HashMap<>();
		Map<MicroService, ZTreeNode> msToZTreeNode = new HashMap<>();
		Set<ZTreeNode> groupZTreeNodes = new HashSet<>();
		JSONObject result = new JSONObject();
		List<CytoscapeNode> nodes = new ArrayList<>();
		List<CytoscapeEdge> edges = new ArrayList<>();
		List<CytoscapeNode> groupNodes = new ArrayList<>();
		List<CytoscapeEdge> groupEdges = new ArrayList<>();
		Map<Node, Boolean> isNodeToCytoscapeNode = new HashMap<>();
		Map<String, Boolean> isIdToCytoscapeEdge = new HashMap<>();
		
		for(FunctionCloneGroup functionGroup : functionGroups) {
			if(!groups.contains(functionGroup.getGroup())) {
				continue;
			}
			CloneGroup cloneGroup = functionGroup.getGroup();
			CytoscapeNode groupNode = new CytoscapeNode(cloneGroup.getId(), cloneGroup.getName(), "CloneGroup");
			groupNodes.add(groupNode);
			groupZTreeNodes.add(new ZTreeNode(cloneGroup.getId(), cloneGroup.getName(), false, "CloneGroup", false));
			
			for(FunctionCloneFunction cloneRelation : functionGroup.getRelations()) {
				Function function1 = cloneRelation.getFunction1();
				if(!isNodeToCytoscapeNode.getOrDefault(function1, false)) {
					isNodeToCytoscapeNode.put(function1, true);
					CytoscapeNode function1CytoscapeNode = new CytoscapeNode(function1.getId(), function1.getName() + "\n(" + function1.getStartLine() + " , " + function1.getEndLine() + ")", "Function");
					function1CytoscapeNode.setValue(function1.getFunctionIdentifier());
					nodes.add(function1CytoscapeNode);
					groupEdges.add(new CytoscapeEdge(function1.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
					nodeToZTreeNode.put(function1, new ZTreeNode(function1.getId(), function1.getFunctionIdentifier() + "(" + function1.getStartLine() + " , " + function1.getEndLine(), false, "Function", false));
				}
				Function function2 = cloneRelation.getFunction2();
				if(!isNodeToCytoscapeNode.getOrDefault(function2, false)) {
					isNodeToCytoscapeNode.put(function2, true);
					CytoscapeNode function2CytoscapeNode = new CytoscapeNode(function2.getId(), function2.getName() + "\n(" + function1.getStartLine() + " , " + function1.getEndLine() + ")", "Function");
					function2CytoscapeNode.setValue(function2.getFunctionIdentifier());
					nodes.add(function2CytoscapeNode);
					groupEdges.add(new CytoscapeEdge(function2.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
					nodeToZTreeNode.put(function2, new ZTreeNode(function2.getId(), function2.getFunctionIdentifier() + "(" + function2.getStartLine() + " , " + function2.getEndLine(), false, "Function", false));
				}
				ProjectFile file1 = containRelationService.findFunctionBelongToFile(function1);
				if(!isNodeToCytoscapeNode.getOrDefault(file1, false)) {
					isNodeToCytoscapeNode.put(file1, true);
					CytoscapeNode file1CytoscapeNode = new CytoscapeNode(file1.getId(), file1.getName() + "\n(" + file1.getLine() + ")", "File");
					file1CytoscapeNode.setValue(file1.getPath());
					nodes.add(file1CytoscapeNode);
					nodeToZTreeNode.put(file1, new ZTreeNode(file1.getId(), file1.getPath() + "(" + file1.getLine() + ")", false, "File", true));
				}
				String file1ContainFunction1Id = String.join("_", String.valueOf(file1.getId()), String.valueOf(function1.getId()));
				if(!isIdToCytoscapeEdge.getOrDefault(file1ContainFunction1Id, false)) {
					isIdToCytoscapeEdge.put(file1ContainFunction1Id, true);
					edges.add(new CytoscapeEdge(file1, function1, "Contain"));
					nodeToZTreeNode.get(file1).addChild(nodeToZTreeNode.get(function1));
				}
				ProjectFile file2 = containRelationService.findFunctionBelongToFile(function2);
				if(!isNodeToCytoscapeNode.getOrDefault(file2, false)) {
					isNodeToCytoscapeNode.put(file2, true);
					CytoscapeNode file2CytoscapeNode = new CytoscapeNode(file2.getId(), file2.getName() + "\n(" + file2.getLine() + ")", "File");
					file2CytoscapeNode.setValue(file2.getPath());
					nodes.add(file2CytoscapeNode);
					nodeToZTreeNode.put(file2, new ZTreeNode(file2.getId(), file2.getPath() + "(" + file2.getLine() + ")", false, "File", true));
				}
				String file2ContainFunction2Id = String.join("_", String.valueOf(file2.getId()), String.valueOf(function2.getId()));
				if(!isIdToCytoscapeEdge.getOrDefault(file2ContainFunction2Id, false)) {
					isIdToCytoscapeEdge.put(file2ContainFunction2Id, true);
					edges.add(new CytoscapeEdge(file2, function2, "Contain"));
					nodeToZTreeNode.get(file2).addChild(nodeToZTreeNode.get(function2));
				}
				Project project1 = containRelationService.findFileBelongToProject(file1);
				MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
				if(ms1 == null) {
					if(!isNodeToCytoscapeNode.getOrDefault(project1, false)) {
						isNodeToCytoscapeNode.put(project1, true);
						nodes.add(new CytoscapeNode(project1.getId(), project1.getName() + "(" + project1.getLanguage().toString() + ")", "Project"));
						projectToZTreeNode.put(project1, new ZTreeNode(project1.getId(), project1.getName() + "(" + project1.getLanguage() + ")", false, "Project", true));
					}
					String project1ContainFile1Id = String.join("_", String.valueOf(project1.getId()), String.valueOf(file1.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(project1ContainFile1Id, false)) {
						isIdToCytoscapeEdge.put(project1ContainFile1Id, true);
						edges.add(new CytoscapeEdge(project1, file1, "Contain"));
						projectToZTreeNode.get(project1).addChild(nodeToZTreeNode.get(file1));
					}
				} else {
					if(!isNodeToCytoscapeNode.getOrDefault(ms1, false)) {
						isNodeToCytoscapeNode.put(ms1, true);
						nodes.add(new CytoscapeNode(ms1.getId(), ms1.getName(), "MicroService"));
						msToZTreeNode.put(ms1, new ZTreeNode(ms1.getId(), ms1.getName(), false, "MicroService", true));
					}
					String ms1ContainFile1Id = String.join("_", String.valueOf(ms1.getId()), String.valueOf(file1.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(ms1ContainFile1Id, false)) {
						isIdToCytoscapeEdge.put(ms1ContainFile1Id, true);
						edges.add(new CytoscapeEdge(ms1, file1, "Contain"));
						msToZTreeNode.get(ms1).addChild(nodeToZTreeNode.get(file1));
					}
				}
				Project project2 = containRelationService.findFileBelongToProject(file2);
				MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
				if(ms2 == null) {
					if(!isNodeToCytoscapeNode.getOrDefault(project2, false)) {
						isNodeToCytoscapeNode.put(project2, true);
						nodes.add(new CytoscapeNode(project2.getId(), project2.getName() + "(" + project2.getLanguage().toString() + ")", "Project"));
						projectToZTreeNode.put(project2, new ZTreeNode(project2.getId(), project2.getName() + "(" + project2.getLanguage() + ")", false, "Project", true));
					}
					String project2ContainFile2Id = String.join("_", String.valueOf(project2.getId()), String.valueOf(file2.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(project2ContainFile2Id, false)) {
						isIdToCytoscapeEdge.put(project2ContainFile2Id, true);
						edges.add(new CytoscapeEdge(project2, file2, "Contain"));
						projectToZTreeNode.get(project2).addChild(nodeToZTreeNode.get(file2));
					}
				} else {
					if(!isNodeToCytoscapeNode.getOrDefault(ms2, false)) {
						isNodeToCytoscapeNode.put(ms2, true);
						nodes.add(new CytoscapeNode(ms2.getId(), ms2.getName(), "MicroService"));
						msToZTreeNode.put(ms2, new ZTreeNode(ms2.getId(), ms2.getName(), false, "MicroService", true));
					}
					String ms2ContainFile2Id = String.join("_", String.valueOf(ms2.getId()), String.valueOf(file2.getId()));
					if(!isIdToCytoscapeEdge.getOrDefault(ms2ContainFile2Id, false)) {
						isIdToCytoscapeEdge.put(ms2ContainFile2Id, true);
						edges.add(new CytoscapeEdge(ms2, file2, "Contain"));
						msToZTreeNode.get(ms2).addChild(nodeToZTreeNode.get(file2));
					}
				}
			}
		}
		

		JSONArray ztreeResult = new JSONArray();
		for(ZTreeNode node : projectToZTreeNode.values()) {
			ztreeResult.add(node.toJSON());
		}
		for(ZTreeNode node : msToZTreeNode.values()) {
			ztreeResult.add(node.toJSON());
		}
		if(showGroupNode) {
			nodes.addAll(groupNodes);
			edges.addAll(groupEdges);
			ZTreeNode groupAllNodes = new ZTreeNode(-1, "所有组", true, "NodeGroup", true);
			for(ZTreeNode node : groupZTreeNodes) {
				groupAllNodes.addChild(node);
			}
			ztreeResult.add(groupAllNodes);
		}
		result.put("ztree", ztreeResult);
		result.put("nodes", CytoscapeUtil.toNodes(nodes));
		result.put("edges", CytoscapeUtil.toEdges(edges));
		return result;
	}
    
	@Override
	public JSONObject clonesGroupsToCytoscape(Collection<CloneGroup> groups, CloneLevel level, 
			boolean showGroupNode,
			boolean removeFileLevelClone, boolean removeDataClass) {
		if(level == CloneLevel.function) {
			return functionClonesGroupsToCytoscape(groups, showGroupNode, removeFileLevelClone);
		} else {
			return fileClonesGroupsToCytoscape(groups, showGroupNode, removeDataClass);
		}
	}
}
