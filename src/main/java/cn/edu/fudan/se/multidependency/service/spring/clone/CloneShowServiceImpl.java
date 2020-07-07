package cn.edu.fudan.se.multidependency.service.spring.clone;

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
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.spring.data.HistogramWithProjectsSize;
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
	
	@Override
	public Collection<HistogramWithProjectsSize> withProjectsSizeToHistogram(Collection<CloneGroup> groups, boolean singleLanguage) {
		Map<Integer, HistogramWithProjectsSize> map = new HashMap<>();
		for(CloneGroup group : groups) {
			int mssSize = 0;
			if(!singleLanguage) {
				Collection<MicroService> mss = cloneAnalyseService.cloneGroupContainMicroServices(group);
				mssSize = mss.size();
			} else {
				Collection<Project> projects = cloneAnalyseService.cloneGroupContainProjects(group);
				mssSize = projects.size();
			}
			if(mssSize == 0) {
				continue;
			}
			HistogramWithProjectsSize histogram = map.getOrDefault(mssSize, new HistogramWithProjectsSize(mssSize));
			histogram.addGroupSize(1);
			histogram.addNodesSize(group.sizeOfNodes());
			map.put(mssSize, histogram);
		}
		return map.values();
	}
	
	@Override
	public JSONObject clonesGroupsToCytoscape(Collection<CloneGroup> groups, boolean showGroupNode, boolean singleLanguage) {
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
		
		for(CloneGroup cloneGroup : groups) {
			CytoscapeNode groupNode = new CytoscapeNode(cloneGroup.getId(), cloneGroup.getName(), "CloneGroup");
			groupNodes.add(groupNode);
			groupZTreeNodes.add(new ZTreeNode(cloneGroup.getId(), cloneGroup.getName(), false, "CloneGroup", false));
			
			for(Clone cloneRelation : cloneGroup.getRelations()) {
				CodeNode node1 = cloneRelation.getCodeNode1();
				CodeNode node2 = cloneRelation.getCodeNode2();
				ProjectFile file1 = null;
				ProjectFile file2 = null;
				if(node1 instanceof ProjectFile && node2 instanceof ProjectFile) {
					file1 = (ProjectFile) node1;
					file2 = (ProjectFile) node2;
					if(!isNodeToCytoscapeNode.getOrDefault(file1, false)) {
						isNodeToCytoscapeNode.put(file1, true);
						CytoscapeNode file1CytoscapeNode = new CytoscapeNode(file1.getId(), file1.getName() + "\n(" + file1.getLines() + ")", "File");
						file1CytoscapeNode.setValue(file1.getPath());
						nodes.add(file1CytoscapeNode);
						nodeToZTreeNode.put(file1, new ZTreeNode(file1.getId(), file1.getPath() + "(" + file1.getLines() + ")", false, "File", true));
					}
					if(!isNodeToCytoscapeNode.getOrDefault(file2, false)) {
						isNodeToCytoscapeNode.put(file2, true);
						CytoscapeNode file2CytoscapeNode = new CytoscapeNode(file2.getId(), file2.getName() + "\n(" + file2.getLines() + ")", "File");
						file2CytoscapeNode.setValue(file2.getPath());
						nodes.add(file2CytoscapeNode);
						nodeToZTreeNode.put(file2, new ZTreeNode(file2.getId(), file2.getPath() + "(" + file2.getLines() + ")", false, "File", true));
					}
					edges.add(new CytoscapeEdge(file1, file2, "Clone", new StringBuilder().append(cloneRelation.getValue()).append(" : ").append(cloneRelation.getCloneType()).toString()));
				} else {
					file1 = containRelationService.findCodeNodeBelongToFile(node1);
					file2 = containRelationService.findCodeNodeBelongToFile(node2);
					if(!isNodeToCytoscapeNode.getOrDefault(file1, false)) {
						isNodeToCytoscapeNode.put(file1, true);
						CytoscapeNode file1CytoscapeNode = new CytoscapeNode(file1.getId(), file1.getName() + "\n(" + file1.getLines() + ")", "File");
						file1CytoscapeNode.setValue(file1.getPath());
						nodes.add(file1CytoscapeNode);
						nodeToZTreeNode.put(file1, new ZTreeNode(file1.getId(), file1.getPath() + "(" + file1.getLines() + ")", false, "File", true));
					}
					if(!isNodeToCytoscapeNode.getOrDefault(file2, false)) {
						isNodeToCytoscapeNode.put(file2, true);
						CytoscapeNode file2CytoscapeNode = new CytoscapeNode(file2.getId(), file2.getName() + "\n(" + file2.getLines() + ")", "File");
						file2CytoscapeNode.setValue(file2.getPath());
						nodes.add(file2CytoscapeNode);
						nodeToZTreeNode.put(file2, new ZTreeNode(file2.getId(), file2.getPath() + "(" + file2.getLines() + ")", false, "File", true));
					}
					if(node1 instanceof Type) {
						Type type1 = (Type) node1;
						if(!isNodeToCytoscapeNode.getOrDefault(type1, false)) {
							isNodeToCytoscapeNode.put(type1, true);
							CytoscapeNode type1CytoscapeNode = new CytoscapeNode(type1.getId(), type1.getName() + "\n(" + type1.getStartLine() + " , " + type1.getEndLine() + ")", "Type");
							type1CytoscapeNode.setValue(type1.getIdentifierSuffix());
							nodes.add(type1CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(type1.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(type1, new ZTreeNode(type1.getId(), type1.getIdentifier() + "(" + type1.getStartLine() + " , " + type1.getEndLine() + ")", false, "Type", false));
						}
						String file1ContainType1Id = String.join("_", String.valueOf(file1.getId()), String.valueOf(type1.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file1ContainType1Id, false)) {
							isIdToCytoscapeEdge.put(file1ContainType1Id, true);
							edges.add(new CytoscapeEdge(file1, type1, "Contain"));
							nodeToZTreeNode.get(file1).addChild(nodeToZTreeNode.get(type1));
						}
					} else if(node1 instanceof Function) {
						Function function1 = (Function) node1;
						if(!isNodeToCytoscapeNode.getOrDefault(function1, false)) {
							isNodeToCytoscapeNode.put(function1, true);
							CytoscapeNode function1CytoscapeNode = new CytoscapeNode(function1.getId(), function1.getName() + "\n(" + function1.getStartLine() + " , " + function1.getEndLine() + ")", "Function");
							function1CytoscapeNode.setValue(function1.getFunctionIdentifier());
							nodes.add(function1CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(function1.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(function1, new ZTreeNode(function1.getId(), function1.getFunctionIdentifier() + "(" + function1.getStartLine() + " , " + function1.getEndLine() + ")", false, "Function", false));
						}
						String file1ContainFunction1Id = String.join("_", String.valueOf(file1.getId()), String.valueOf(function1.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file1ContainFunction1Id, false)) {
							isIdToCytoscapeEdge.put(file1ContainFunction1Id, true);
							edges.add(new CytoscapeEdge(file1, function1, "Contain"));
							nodeToZTreeNode.get(file1).addChild(nodeToZTreeNode.get(function1));
						}
					} else if(node1 instanceof Snippet) {
						Snippet snippet1 = (Snippet) node1;
						if(!isNodeToCytoscapeNode.getOrDefault(snippet1, false)) {
							isNodeToCytoscapeNode.put(snippet1, true);
							CytoscapeNode snippet1CytoscapeNode = new CytoscapeNode(snippet1.getId(), snippet1.getName(), "Snippet");
							snippet1CytoscapeNode.setValue(snippet1.getIdentifier());
							nodes.add(snippet1CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(snippet1.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(snippet1, new ZTreeNode(snippet1.getId(), snippet1.getIdentifier(), false, "Snippet", false));
						}
						String file1ContainSnippet1Id = String.join("_", String.valueOf(file1.getId()), String.valueOf(snippet1.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file1ContainSnippet1Id, false)) {
							isIdToCytoscapeEdge.put(file1ContainSnippet1Id, true);
							edges.add(new CytoscapeEdge(file1, snippet1, "Contain"));
							nodeToZTreeNode.get(file1).addChild(nodeToZTreeNode.get(snippet1));
						}
					}
					if(node2 instanceof Type) {
						Type type2 = (Type) node2;
						if(!isNodeToCytoscapeNode.getOrDefault(type2, false)) {
							isNodeToCytoscapeNode.put(type2, true);
							CytoscapeNode type1CytoscapeNode = new CytoscapeNode(type2.getId(), type2.getName() + "\n(" + type2.getStartLine() + " , " + type2.getEndLine() + ")", "Type");
							type1CytoscapeNode.setValue(type2.getIdentifierSuffix());
							nodes.add(type1CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(type2.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(type2, new ZTreeNode(type2.getId(), type2.getIdentifier() + "(" + type2.getStartLine() + " , " + type2.getEndLine() + ")", false, "Type", false));
						}
						String file2ContainType2Id = String.join("_", String.valueOf(file2.getId()), String.valueOf(type2.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file2ContainType2Id, false)) {
							isIdToCytoscapeEdge.put(file2ContainType2Id, true);
							edges.add(new CytoscapeEdge(file2, type2, "Contain"));
							nodeToZTreeNode.get(file2).addChild(nodeToZTreeNode.get(type2));
						}
					} else if(node2 instanceof Function) {
						Function function2 = (Function) node2;
						if(!isNodeToCytoscapeNode.getOrDefault(function2, false)) {
							isNodeToCytoscapeNode.put(function2, true);
							CytoscapeNode function2CytoscapeNode = new CytoscapeNode(function2.getId(), function2.getName() + "\n(" + function2.getStartLine() + " , " + function2.getEndLine() + ")", "Function");
							function2CytoscapeNode.setValue(function2.getFunctionIdentifier());
							nodes.add(function2CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(function2.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(function2, new ZTreeNode(function2.getId(), function2.getFunctionIdentifier() + "(" + function2.getStartLine() + " , " + function2.getEndLine() + ")", false, "Function", false));
						}
						String file2ContainFunction2Id = String.join("_", String.valueOf(file2.getId()), String.valueOf(function2.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file2ContainFunction2Id, false)) {
							isIdToCytoscapeEdge.put(file2ContainFunction2Id, true);
							edges.add(new CytoscapeEdge(file2, function2, "Contain"));
							nodeToZTreeNode.get(file2).addChild(nodeToZTreeNode.get(function2));
						}
					} else if(node2 instanceof Snippet) {
						Snippet snippet2 = (Snippet) node2;
						if(!isNodeToCytoscapeNode.getOrDefault(snippet2, false)) {
							isNodeToCytoscapeNode.put(snippet2, true);
							CytoscapeNode snippet2CytoscapeNode = new CytoscapeNode(snippet2.getId(), snippet2.getName(), "Snippet");
							snippet2CytoscapeNode.setValue(snippet2.getIdentifier());
							nodes.add(snippet2CytoscapeNode);
							groupEdges.add(new CytoscapeEdge(snippet2.getId().toString(), cloneGroup.getId().toString(), "nodeIsInCloneGroup"));
							nodeToZTreeNode.put(snippet2, new ZTreeNode(snippet2.getId(), snippet2.getIdentifier(), false, "Snippet", false));
						}
						String file2ContainSnippet2Id = String.join("_", String.valueOf(file2.getId()), String.valueOf(snippet2.getId()));
						if(!isIdToCytoscapeEdge.getOrDefault(file2ContainSnippet2Id, false)) {
							isIdToCytoscapeEdge.put(file2ContainSnippet2Id, true);
							edges.add(new CytoscapeEdge(file2, snippet2, "Contain"));
							nodeToZTreeNode.get(file2).addChild(nodeToZTreeNode.get(snippet2));
						}
					}
					
//					edges.add(new CytoscapeEdge(node1, node2, "Clone", String.valueOf(cloneRelation.getValue())));
					edges.add(new CytoscapeEdge(node1, node2, "Clone", new StringBuilder().append(cloneRelation.getValue()).append(" : ").append(cloneRelation.getCloneType()).toString()));
				}
				
				Project project1 = containRelationService.findFileBelongToProject(file1);
				if(!singleLanguage) {
					MicroService ms1 = containRelationService.findProjectBelongToMicroService(project1);
					if(ms1 != null) {
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
				} else {
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
				}
				Project project2 = containRelationService.findFileBelongToProject(file2);
				if(!singleLanguage) {
					MicroService ms2 = containRelationService.findProjectBelongToMicroService(project2);
					if(ms2 != null) {
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
				} else {
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
}