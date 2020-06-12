package cn.edu.fudan.se.multidependency.service.spring.data;

import java.util.HashSet;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import lombok.Data;

@Data
public class FunctionCloneGroup implements IsCloneGroup {
	
	public FunctionCloneGroup(CloneGroup group) {
		this.group = group;
	}

	private CloneGroup group;
	
	private Set<Function> nodes = new HashSet<>();
	
	private Set<FunctionCloneFunction> relations = new HashSet<>();
	
	/*private Map<Function, Project> functionBelongToProject = new HashMap<>();
	
	private Map<Function, MicroService> functionBelongToMSs = new HashMap<>();
	
	public void addFunctionBelongToProject(Function function, Project project) {
		this.functionBelongToProject.put(function, project);
	}
	
	public void addFunctionBelongToMicroService(Function function, MicroService ms) {
		this.functionBelongToMSs.put(function, ms);
	}
	
	public Collection<MicroService> relatedMSs() {
		Set<MicroService> result = new HashSet<>();
		result.addAll(functionBelongToMSs.values());
		return result;
	}
	
	public Collection<Project> relatedProjects() {
		Set<Project> result = new HashSet<>();
		result.addAll(functionBelongToProject.values());
		return result;
	}*/
	
	public void addFunction(Function function) {
		this.nodes.add(function);
	}
	
	public void addRelation(FunctionCloneFunction relation) {
		this.relations.add(relation);
	}
	
}
