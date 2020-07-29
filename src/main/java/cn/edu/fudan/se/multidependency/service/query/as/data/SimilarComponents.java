package cn.edu.fudan.se.multidependency.service.query.as.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;

@Data
public class SimilarComponents<T extends Node> {

	List<T> components = new ArrayList<>();
	
	public void add(T component) {
		this.components.add(component);
	}
	
	public void addAll(Collection<T> components) {
		this.components.addAll(components);
	}
	
}
