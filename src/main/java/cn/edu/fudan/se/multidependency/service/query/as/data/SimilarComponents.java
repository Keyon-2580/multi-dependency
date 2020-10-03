package cn.edu.fudan.se.multidependency.service.query.as.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import lombok.Data;

@Data
public class SimilarComponents<T extends Node> {

	public SimilarComponents(T node1, T node2, double value, int node1ChangeTimes, int node2ChangeTimes, int cochangeTimes) {
		this.node1 = node1;
		this.node2 = node2;
		this.value = value;
		this.node1ChangeTimes = node1ChangeTimes;
		this.node2ChangeTimes = node2ChangeTimes;
		this.cochangeTimes = cochangeTimes;
	}
	
	private T node1;
	
	private T node2;
	
	private double value;
	
	private String cloneType;
	
	private Module module1;
	
	private Module module2;
	
	private int node1ChangeTimes;
	
	private int node2ChangeTimes;
	
	private int cochangeTimes;
	
}
