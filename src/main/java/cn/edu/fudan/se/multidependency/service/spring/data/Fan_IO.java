package cn.edu.fudan.se.multidependency.service.spring.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;

@Data
public class FAN_IO<T extends Node> implements Serializable {
	
	private static final long serialVersionUID = 110963529785572680L;

	T node;

	private List<T> fanIn = new ArrayList<>();
	
	private List<T> fanOut = new ArrayList<>();
	
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
	
	public FAN_IO(T node) {
		this.node = node;
	}
	
	
}
