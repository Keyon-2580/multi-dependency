package cn.edu.fudan.se.multidependency.model.relation.git;

import java.io.Serializable;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import lombok.Data;

@Data
public class DeveloperUpdateNode<T extends Node> implements Serializable {

	private static final long serialVersionUID = -293939642400094553L;

	private Developer developer;
	
	private T node;
	
	private int times;
	
}
