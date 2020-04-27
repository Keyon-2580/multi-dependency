package cn.edu.fudan.se.multidependency.model.relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;

@Data
public class DependOn<S extends Node, T extends Node> implements Serializable {
	
	private static final long serialVersionUID = -1157441684129004481L;

	private S source;
	
	private T target;
	
	private Collection<Relation> children = new ArrayList<>();

}
