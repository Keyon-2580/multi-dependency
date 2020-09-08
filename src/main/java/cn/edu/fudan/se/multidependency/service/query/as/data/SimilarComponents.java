package cn.edu.fudan.se.multidependency.service.query.as.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import lombok.Data;

@Data
public class SimilarComponents<T extends Node> {

	public SimilarComponents(T node1, T node2, double value, int cochangeTimes) {
		this.node1 = node1;
		this.node2 = node2;
		this.value = value;
		this.cochangeTimes = cochangeTimes;
	}
	
	private T node1;
	
	private T node2;
	
	private double value;
	
	private Module module1;
	
	private Module module2;
	
	private int cochangeTimes;
	
}
