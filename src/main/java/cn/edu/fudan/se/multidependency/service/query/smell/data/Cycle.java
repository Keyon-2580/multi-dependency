package cn.edu.fudan.se.multidependency.service.query.smell.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import lombok.Data;

@Data
public class Cycle<T extends Node> {

	private CycleComponents<T> components;
	
	private List<DependsOn> relations = new ArrayList<>();
	
	private Map<Long, Node> componentToGroup = new HashMap<>();
	
	public Set<Node> getGroups() {
		return new HashSet<>(componentToGroup.values());
	}
	
	public void putComponentBelongToGroup(T node, Node group) {
		this.componentToGroup.put(node.getId(), group);
	}
	
	public Cycle(CycleComponents<T> components) {
		this.components = components;
	}
	
	public void addDependsOn(DependsOn relation) {
		this.relations.add(relation);
	}
	
	public void addAll(Collection<DependsOn> relations) {
		this.relations.addAll(relations);
	}
	
	public Collection<T> getComponents() {
		return components.getComponents();
	}
	
	public int getPartition() {
		return components.getPartition();
	}
	
}
