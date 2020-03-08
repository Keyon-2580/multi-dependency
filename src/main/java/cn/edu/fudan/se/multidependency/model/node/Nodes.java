package cn.edu.fudan.se.multidependency.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;

public class Nodes {
	
	private List<Node> allNodes = new ArrayList<>();

	private Map<NodeType, List<Node>> nodeTypeToNodes = new HashMap<>();
	
	/**
	 * 每个项目内的节点的entityId是唯一的
	 */
	private Map<Project, Map<NodeType, Map<Long, Node>>> projectToNodes = new HashMap<>();
	
	private List<Project> projects = new ArrayList<>();
	
	public List<Project> findAllProjects() {
		return new ArrayList<>(projects);
	}
	
	public Project findProject(String name, Language language) {
		if(language == null) {
			return null;
		}
		for(Project project : projects) {
			if(project.getProjectName().equals(name) && project.getLanguage().equals(language.toString())) {
				return project;
			}
		}
		return null;
	}
	
	public void clear() {
		allNodes.clear();
		nodeTypeToNodes.clear();
		projectToNodes.clear();
		projects.clear();
	}
	
	public Map<NodeType, List<Node>> getAllNodes() {
		return new HashMap<>(nodeTypeToNodes);
	}
	
	public int size() {
		return allNodes.size();
	}

	/**
	 * 表示该节点属于哪个Project，Project可以为null
	 * @param node
	 * @param inProject 
	 * @return
	 */
	public void addNode(Node node, Project inProject) {
		allNodes.add(node);
		if(node instanceof Project) {
			projects.add((Project) node);
		}
		List<Node> nodes = nodeTypeToNodes.get(node.getNodeType());
		nodes = nodes == null ? new ArrayList<>() : nodes;
		nodes.add(node);
		nodeTypeToNodes.put(node.getNodeType(), nodes);
		
		if(inProject != null && projects.contains(inProject)) {
			Map<NodeType, Map<Long, Node>> projectHasNodes = projectToNodes.get(inProject);
			projectHasNodes = projectHasNodes == null ? new HashMap<>() : projectHasNodes;
			Map<Long, Node> entityIdToNode = projectHasNodes.get(node.getNodeType());
			entityIdToNode = entityIdToNode == null ? new HashMap<>() : entityIdToNode;
			entityIdToNode.put(node.getEntityId(), node);
			projectHasNodes.put(node.getNodeType(), entityIdToNode);
			projectToNodes.put(inProject, projectHasNodes);
		}
	}
	
	public List<? extends Node> findNodesByNodeType(NodeType nodeType) {
		List<? extends Node> result = nodeTypeToNodes.get(nodeType);
		return result == null ? new ArrayList<>() : result;
	}
	
	public Node findNodeByEntityIdInProject(NodeType nodeType, Long entityId, Project inProject) {
		Map<Long, ? extends Node> nodes = findNodesByNodeTypeInProject(nodeType, inProject);
		if(nodes.get(entityId) != null) {
			return nodes.get(entityId).getNodeType() == nodeType ? nodes.get(entityId) : null;
		}
		return null;
	}

	public Node findNodeByEntityIdInProject(Long entityId, Project inProject) {
		Map<NodeType, Map<Long, Node>> typeToNodes = projectToNodes.get(inProject);
		if(typeToNodes == null) {
			return null;
		}
		for(Map<Long, Node> entityIdToNode : typeToNodes.values()) {
			if(entityIdToNode.get(entityId) != null) {
				return entityIdToNode.get(entityId);
			}
		}
		return null;
	}
	
	public Map<Long, ? extends Node> findNodesByNodeTypeInProject(NodeType nodeType, Project inProject) {
		Map<NodeType, Map<Long, Node>> projectHasNodes = projectToNodes.get(inProject);
		if(projectHasNodes == null) {
			return new HashMap<>();
		}
		Map<Long, ? extends Node> result = projectHasNodes.get(nodeType);
		return result == null ? new HashMap<>() : result;
	}
	
	/**
	 * 某节点是否存在
	 * @param node
	 * @return
	 */
	public boolean existNode(Node node) {
		return allNodes.contains(node);
	}
	
	public Map<String, List<Function>> findFunctionsInProject(Project project) {
		Map<String, List<Function>> result = new HashMap<>();
		if(project == null) {
			return result;
		}
		@SuppressWarnings("unchecked")
		Map<Long, Function> functions = (Map<Long, Function>) findNodesByNodeTypeInProject(NodeType.Function, project);
		functions.values().forEach(function -> {
			String functionName = function.getFunctionName();
			List<Function> fs = result.get(functionName);
			fs = fs == null ? new ArrayList<>() : fs;
			fs.add(function);
			result.put(functionName, fs);
		});
		
		return result;
	}
	
	public Package findPackageByPackageName(String packageName, Project project) {
		@SuppressWarnings("unchecked")
		Map<Long, Package> packages = (Map<Long, Package>) findNodesByNodeTypeInProject(NodeType.Package, project);
		for(Package pck : packages.values()) {
			if(pck.getPackageName().equals(packageName)) {
				return pck;
			}
		}
		return null;
	}

	public Map<Long, Scenario> findScenarios() {
		Map<Long, Scenario> scenarios = new HashMap<>();
		findNodesByNodeType(NodeType.Scenario).forEach(node -> {
			scenarios.put(node.getId(), (Scenario) node);
		});
		return scenarios;
	}
	
	public Scenario findScenarioByName(String scenarioName) {
		for(Scenario s : findScenarios().values()) {
			if(s.getScenarioName().equals(scenarioName)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * featureId to feature
	 * @return
	 */
	public Map<Integer, Feature> findFeatures() {
		Map<Integer, Feature> features = new HashMap<>();
		findNodesByNodeType(NodeType.Feature).forEach(node -> {
			Feature feature = (Feature) node;
			features.put(feature.getFeatureId(), (Feature) node);
		});
		return features;
	}
	
	/**
	 * traceId to trace
	 * @return
	 */
	public Map<String, Trace> findTraces() {
		Map<String, Trace> traces = new HashMap<>();
		findNodesByNodeType(NodeType.Trace).forEach(node -> {
			Trace trace = (Trace) node;
			traces.put(trace.getTraceId(), trace);
		});
		return traces;
	}
	
	public MicroService findMicroServiceByName(String name) {
		for(Node node : findNodesByNodeType(NodeType.MicroService)) {
			MicroService temp = (MicroService) node;
			if(name.equals(temp.getName())) {
				return temp;
			}
		}
		return null;
	}

	public Span findSpanBySpanId(String spanId) {
		for(Node node : findNodesByNodeType(NodeType.Span)) {
			Span span = (Span) node;
			if(spanId.equals(span.getSpanId())) {
				return span;
			}
		}
		return null;
	}
	
}
