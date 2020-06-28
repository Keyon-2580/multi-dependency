package cn.edu.fudan.se.multidependency.service.spring.metric;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import lombok.Data;

@Data
public class Fan_IO<T extends Node> implements Serializable {
	
	private static final long serialVersionUID = 110963529785572680L;

	T node;

	private List<T> fanIn = new ArrayList<>();
	
	private List<T> fanOut = new ArrayList<>();
	
	private Collection<Relation> fanInRelations = new ArrayList<>();
	
	private Collection<Relation> fanOutRelations = new ArrayList<>();
	
	public void addFanInRelations(Relation relation) {
		fanInRelations.add(relation);
	}
	
	public void addFanOutRelations(Relation relation) {
		fanOutRelations.add(relation);
	}
	
	public T getNode() {
		return node;
	}
	
	public int sizeOfFanIn() {
		return fanIn.size();
	}
	
	public int sizeOfFanOut() {
		return fanOut.size();
	}
	
	public int size() {
		return sizeOfFanIn() + sizeOfFanOut();
	}
	
	public void addFanIn(T in) {
		this.fanIn.add(in);
	}
	
	public void addFanOut(T out) {
		this.fanOut.add(out);
	}
	
	public Fan_IO(T node) {
		this.node = node;
	}
	
	
}
