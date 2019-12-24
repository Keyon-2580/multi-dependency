package fan.md.model.node.code;

import java.util.Map;

import fan.md.model.node.Node;
import fan.md.model.node.NodeType;
import fan.md.model.node.Nodes;
import fan.md.model.node.Project;

public class StaticCodeNodes extends Nodes {
	
	private Project project;
	
	public Package findPackage(Integer entityId) {
		Node node = findNode(entityId, NodeType.Package);
		return node == null ? null : (Package) node;
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
	
	public Function findFunction(Integer entityId) {
		Node node = findNode(entityId, NodeType.Function);
		return node == null ? null : (Function) node;
	}
	
	public CodeFile findCodeFile(Integer entityId) {
		Node node = findNode(entityId, NodeType.File);
		return node == null ? null : (CodeFile) node;
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
