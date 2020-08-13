package cn.edu.fudan.se.multidependency.service.query.as.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import lombok.Data;

@Data
public class Cycle<T extends Node> {

	private CycleComponents<T> components;
	
	private List<DependsOn> relations = new ArrayList<>();
	
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
