package cn.edu.fudan.se.multidependency.model.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;

public class Nodes {

	private Map<NodeType, Map<Long, Node>> allNodes = new HashMap<>();
	
	public void clear() {
		allNodes.clear();
	}
	
	public Map<NodeType, Map<Long, Node>> getAllNodes() {
		return new HashMap<>(allNodes);
	}
	
	public int size() {
		int size = 0;
		for(Map<Long, Node> nodes : allNodes.values()) {
			size += nodes.size();
		}
		return size;
	}
	
	public void clear(NodeType nodeType) {
		Map<Long, Node> nodes = allNodes.get(nodeType);
		if(nodes != null) {
			nodes.clear();
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param entityId
	 */
	public void addNode(Node node) {
		Map<Long, Node> nodes = allNodes.get(node.getNodeType());
		nodes = nodes == null ? new HashMap<>() : nodes;
		nodes.put(node.getEntityId(), node);
		allNodes.put(node.getNodeType(), nodes);
	}
	
	/**
	 * 根据entityId查找node
	 * @param entityId
	 * @return
	 */
	public Node findNode(Long entityId) {
		for(Map<Long, Node> nodes : allNodes.values()) {
			Node node = nodes.get(entityId);
			if(nodes.get(entityId) != null) {
				return node;
			}
		}
		return null;
	}
	
	public Node findNode(Long entityId, NodeType nodeType) {
		Map<Long, Node> nodes = allNodes.get(nodeType);
		if(nodes == null) {
			return null;
		}
		return nodes.get(entityId);
	}
	
	public Collection<? extends Node> findNodesCollection(NodeType nodeType) {
		Map<Long, Node> nodes = allNodes.get(nodeType);
		return nodes == null ? new ArrayList<>() : nodes.values();
	}
	
	public Map<Long, ? extends Node> findNodesMap(NodeType nodeType) {
		Map<Long, ? extends Node> nodes = allNodes.get(nodeType);
		return nodes == null ? new HashMap<>() : nodes;
	}
	
private Project project;
	
	public Package findPackage(Long entityId) {
		Node node = findNode(entityId, NodeType.Package);
		return node == null ? null : (Package) node;
	}
	
	public Package findPackageByPackageName(String packageName) {
		Map<Long, Package> packages = findPackages();
		for(Package pck : packages.values()) {
			if(packageName.equals(pck.getPackageName())) {
				return pck;
			}
		}
		return null;
	}
	
	public Type findType(Long entityId) {
		Node node = findNode(entityId, NodeType.Type);
		return node == null ? null : (Type) node;
	}
	
	public Map<Long, Node> findNodes(Class<? extends Node> cls) {
		return null;
	}
	
	public Map<Long, Type> findTypes() {
		Map<Long, Type> types = (Map<Long, Type>) findNodesMap(NodeType.Type);
		return types;
	}
	
	public Map<Long, Package> findPackages() {
		Map<Long, Package> packages = (Map<Long, Package>) findNodesMap(NodeType.Package);
		return packages;
	}
	
	public Map<Long, Function> findFunctions() {
		Map<Long, Function> functions = (Map<Long, Function>) findNodesMap(NodeType.Function);
		return functions;
	}
	
	public Map<String, List<Function>> allFunctionsByFunctionName() {
		Map<String, List<Function>> result = new HashMap<>();
		for(Function function : findFunctions().values()) {
			String functionName = function.getFunctionName();
			List<Function> functions = result.get(functionName);
			functions = functions == null ? new ArrayList<>() : functions;
			functions.add(function);
			result.put(functionName, functions);
		}
		return result;
	}
	
	public Map<Long, ProjectFile> findFiles() {
		Map<Long, ProjectFile> files = (Map<Long, ProjectFile>) findNodesMap(NodeType.ProjectFile);
		return files;
	}
	
	public Map<Long, Variable> findVariables() {
		Map<Long, Variable> variables = (Map<Long, Variable>) findNodesMap(NodeType.Variable);
		return variables;
	}
	
	public Function findFunction(Long entityId) {
		Node node = findNode(entityId, NodeType.Function);
		return node == null ? null : (Function) node;
	}
	
	public ProjectFile findCodeFile(Long entityId) {
		Node node = findNode(entityId, NodeType.ProjectFile);
		return node == null ? null : (ProjectFile) node;
	}
	
	public Variable findVariable(Long entityId) {
		Node node = findNode(entityId, NodeType.Variable);
		return node == null ? null : (Variable) node;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	

	public Map<Long, Scenario> findScenarios() {
		Map<Long, Scenario> scenarios = (Map<Long, Scenario>) findNodesMap(NodeType.Scenario);
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
	
	public Map<Long, Feature> findFeatures() {
		Map<Long, Feature> features = (Map<Long, Feature>) findNodesMap(NodeType.Feature);
		return features;
	}
	
	public Feature findFeatureByFeature(String featureName) {
		for(Feature f : findFeatures().values()) {
			if(f.getFeatureName().equals(featureName)) {
				return f;
			}
		}
		return null;
	}
	
	public Map<Long, TestCase> findTestCases() {
		Map<Long, TestCase> testCases = (Map<Long, TestCase>) findNodesMap(NodeType.TestCase);
		return testCases;
	}

	public TestCase findTestCaseByName(String name) {
		for(TestCase testCase : findTestCases().values()) {
			if(testCase.getTestCaseName().equals(name)) {
				return testCase;
			}
		}
		return null;
	}
	
	public ProjectFile findCodeFileByPath(String projectPath) {
		for(ProjectFile file : findFiles().values()) {
			if(file.getPath().equals(projectPath)) {
				return file;
			}
		}
		return null;
	}

}
