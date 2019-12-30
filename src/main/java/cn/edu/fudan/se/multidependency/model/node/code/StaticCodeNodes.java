package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import cn.edu.fudan.se.multidependency.model.node.Nodes;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;

public class StaticCodeNodes extends Nodes {
	
	private Project project;
	
	public Package findPackage(Integer entityId) {
		Node node = findNode(entityId, NodeType.Package);
		return node == null ? null : (Package) node;
	}
	
	public Package findPackageByPackageName(String packageName) {
		Map<Integer, Package> packages = findPackages();
		for(Package pck : packages.values()) {
			if(packageName.equals(pck.getPackageName())) {
				return pck;
			}
		}
		return null;
	}
	
	public Type findType(Integer entityId) {
		Node node = findNode(entityId, NodeType.Type);
		return node == null ? null : (Type) node;
	}
	
	public Map<Integer, Type> findTypes() {
		Map<Integer, Type> types = (Map<Integer, Type>) findNodesMap(NodeType.Type);
		return types;
	}
	
	public Map<Integer, Package> findPackages() {
		Map<Integer, Package> packages = (Map<Integer, Package>) findNodesMap(NodeType.Package);
		return packages;
	}
	
	public Map<Integer, Function> findFunctions() {
		Map<Integer, Function> functions = (Map<Integer, Function>) findNodesMap(NodeType.Function);
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
	
	public Map<Integer, ProjectFile> findFiles() {
		Map<Integer, ProjectFile> files = (Map<Integer, ProjectFile>) findNodesMap(NodeType.File);
		return files;
	}
	
	public Map<Integer, Variable> findVariables() {
		Map<Integer, Variable> variables = (Map<Integer, Variable>) findNodesMap(NodeType.Variable);
		return variables;
	}
	
	public Function findFunction(Integer entityId) {
		Node node = findNode(entityId, NodeType.Function);
		return node == null ? null : (Function) node;
	}
	
	public ProjectFile findCodeFile(Integer entityId) {
		Node node = findNode(entityId, NodeType.File);
		return node == null ? null : (ProjectFile) node;
	}
	
	public Variable findVariable(Integer entityId) {
		Node node = findNode(entityId, NodeType.Variable);
		return node == null ? null : (Variable) node;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
