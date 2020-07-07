package cn.edu.fudan.se.multidependency.model.relation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;

public interface StructureRelation extends Relation {
	
	CodeNode getStartCodeNode();
	
	CodeNode getEndCodeNode();
	
	default Node getStartNode() {
		return getStartCodeNode();
	}
	
	default Node getEndNode() {
		return getEndCodeNode();
	}
}
