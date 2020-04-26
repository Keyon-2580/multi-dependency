package cn.edu.fudan.se.multidependency.model.relation;

import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Getter;
import lombok.Setter;

public class Clone {

	@Getter
	@Setter
	private Node node1;
	
	@Getter
	@Setter
	private Node node2;
	
	@Getter
	private double value;
	
	private List<Node> node1ContainNodes = new ArrayList<>();
	
	private List<Node> node2ContainNodes = new ArrayList<>();
	
//	public boolean setNode1(Node node, Collection<Node> containNodes, ) {
//		node1ContainNodes.addAll(containNodes);
//	}
//	
//	public void setNode2(Node node, Collection<Node> containNode) {
//		this.setNode2(node);
//		node2ContainNodes.addAll(containNode);
//	}
	
	
}
