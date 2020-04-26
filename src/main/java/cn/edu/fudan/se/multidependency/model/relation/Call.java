package cn.edu.fudan.se.multidependency.model.relation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;

@Data
public class Call {

	private Node startNode;
	
	private Node endNode;
	
	private int times;
	
}
