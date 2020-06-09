package cn.edu.fudan.se.multidependency.service.spring.data;

import java.util.HashSet;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import lombok.Data;

@Data
public class FunctionCloneGroup {
	
	public FunctionCloneGroup(CloneGroup group) {
		this.group = group;
	}

	private CloneGroup group;
	
	private Set<Function> functions = new HashSet<>();
	
	private Set<FunctionCloneFunction> relations = new HashSet<>();
	
	public void addFunction(Function function) {
		this.functions.add(function);
	}
	
	public void addRelation(FunctionCloneFunction relation) {
		this.relations.add(relation);
	}
	
	public int sizeOfFiles() {
		return functions.size();
	}
	
	public int sizeOfRelations() {
		return relations.size();
	}
	
	
}
