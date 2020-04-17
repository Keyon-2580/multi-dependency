package cn.edu.fudan.se.multidependency.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.MicroService;
import cn.edu.fudan.se.multidependency.model.node.microservice.RestfulAPI;
import cn.edu.fudan.se.multidependency.model.node.microservice.Span;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.node.testcase.Trace;

public class Nodes {
	
	private List<Node> allNodes = new ArrayList<>();

	private Map<NodeLabelType, List<Node>> nodeTypeToNodes = new HashMap<>();
	
	/**
	 * 每个项目内的节点的entityId是唯一的
	 */
	private Map<Project, Map<NodeLabelType, Map<Long, Node>>> projectToNodes = new HashMap<>();
	
	private List<Project> projects = new ArrayList<>();
	
	public List<Project> findAllProjects() {
		return new ArrayList<>(projects);
	}
	
	public Project findProject(String name, Language language) {
		if(language == null) {
			return null;
		}
		for(Project project : projects) {
			if(project.getName().equals(name) && project.getLanguage().equals(language.toString())) {
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
	
	public Map<NodeLabelType, List<Node>> getAllNodes() {
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
		List<Node> nodes = nodeTypeToNodes.getOrDefault(node.getNodeType(), new ArrayList<>());
		nodes.add(node);
		nodeTypeToNodes.put(node.getNodeType(), nodes);
		
		if(inProject != null && projects.contains(inProject)) {
			Map<NodeLabelType, Map<Long, Node>> projectHasNodes = projectToNodes.getOrDefault(inProject, new HashMap<>());
			Map<Long, Node> entityIdToNode = projectHasNodes.getOrDefault(node.getNodeType(), new HashMap<>());
			entityIdToNode.put(node.getEntityId(), node);
			projectHasNodes.put(node.getNodeType(), entityIdToNode);
			projectToNodes.put(inProject, projectHasNodes);
		}
	}
	
	public List<? extends Node> findNodesByNodeType(NodeLabelType nodeType) {
		return nodeTypeToNodes.getOrDefault(nodeType, new ArrayList<>());
	}
	
	public Node findNodeByEntityIdInProject(NodeLabelType nodeType, Long entityId, Project inProject) {
		Map<Long, ? extends Node> nodes = findNodesByNodeTypeInProject(nodeType, inProject);
		if(nodes.get(entityId) != null) {
			return nodes.get(entityId).getNodeType() == nodeType ? nodes.get(entityId) : null;
		}
		return null;
	}

	public Node findNodeByEntityIdInProject(Long entityId, Project inProject) {
		Map<NodeLabelType, Map<Long, Node>> typeToNodes = projectToNodes.get(inProject);
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
	
	/**
	 * 在给定project中查找指定节点类型的所有节点
	 * entity : node
	 * @param nodeType
	 * @param inProject
	 * @return
	 */
	public Map<Long, ? extends Node> findNodesByNodeTypeInProject(NodeLabelType nodeType, Project inProject) {
		Map<NodeLabelType, Map<Long, Node>> projectHasNodes = projectToNodes.getOrDefault(inProject, new HashMap<>());
		return projectHasNodes.getOrDefault(nodeType, new HashMap<>());
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
		Map<Long, Function> functions = (Map<Long, Function>) findNodesByNodeTypeInProject(NodeLabelType.Function, project);
		functions.values().forEach(function -> {
			String functionName = function.getName();
			List<Function> fs = result.getOrDefault(functionName, new ArrayList<>());
			fs.add(function);
			result.put(functionName, fs);
		});
		
		return result;
	}
	
	public Package findPackageByDirectoryPath(String directoryPath, Project project) {
		@SuppressWarnings("unchecked")
		Map<Long, Package> packages = (Map<Long, Package>) findNodesByNodeTypeInProject(NodeLabelType.Package, project);
		System.out.println(packages.size() + " " + packages + " " + directoryPath);
		for(Package pck : packages.values()) {
//			System.out.println(pck.getDirectoryPath() + " " + directoryPath);
			if(pck.getDirectoryPath().equals(directoryPath)) {
				return pck;
			}
		}
		return null;
	}

	public Map<Integer, Scenario> findScenarios() {
		Map<Integer, Scenario> scenarios = new HashMap<>();
		findNodesByNodeType(NodeLabelType.Scenario).forEach(node -> {
			Scenario scenario = (Scenario) node;
			scenarios.put(scenario.getScenarioId(), scenario);
		});
		return scenarios;
	}
	
	public Map<Integer, TestCase> findTestCases() {
		Map<Integer, TestCase> features = new HashMap<>();
		findNodesByNodeType(NodeLabelType.TestCase).forEach(node -> {
			TestCase testcase = (TestCase) node;
			features.put(testcase.getTestCaseId(), testcase);
		});
		return features;
	}
	
	/**
	 * featureId to feature
	 * @return
	 */
	public Map<Integer, Feature> findFeatures() {
		Map<Integer, Feature> features = new HashMap<>();
		findNodesByNodeType(NodeLabelType.Feature).forEach(node -> {
			Feature feature = (Feature) node;
			features.put(feature.getFeatureId(), feature);
		});
		return features;
	}
	
	/**
	 * traceId to trace
	 * @return
	 */
	public Map<String, Trace> findTraces() {
		Map<String, Trace> traces = new HashMap<>();
		findNodesByNodeType(NodeLabelType.Trace).forEach(node -> {
			Trace trace = (Trace) node;
			traces.put(trace.getTraceId(), trace);
		});
		return traces;
	}
	
	public MicroService findMicroServiceByName(String name) {
		for(Node node : findNodesByNodeType(NodeLabelType.MicroService)) {
			MicroService temp = (MicroService) node;
			if(name.equals(temp.getName())) {
				return temp;
			}
		}
		return null;
	}
	
	public RestfulAPI findRestfulAPIByProjectAndSimpleFunctionName(Project project, String simpleFunctionName) {
		Map<Long, ? extends Node> nodes = findNodesByNodeTypeInProject(NodeLabelType.RestfulAPI, project);
		for(Node node : nodes.values()) {
			RestfulAPI api = (RestfulAPI) node;
			if(simpleFunctionName.equals(api.getApiFunctionSimpleName())) {
				return api;
			}
		}
		return null;
	}
	
	public RestfulAPI findMicroServiceAPIByAPIFunction(String apiFunctionName) {
		for(Node node :findNodesByNodeType(NodeLabelType.RestfulAPI)) {
			RestfulAPI api = (RestfulAPI) node;
			if(apiFunctionName.equals(api.getApiFunctionName())) {
				return api;
			}
		}
		return null;
	}

	public Span findSpanBySpanId(String spanId) {
		for(Node node : findNodesByNodeType(NodeLabelType.Span)) {
			Span span = (Span) node;
			if(spanId.equals(span.getSpanId())) {
				return span;
			}
		}
		return null;
	}
	
}
