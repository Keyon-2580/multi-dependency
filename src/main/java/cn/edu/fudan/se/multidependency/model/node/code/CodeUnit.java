package cn.edu.fudan.se.multidependency.model.node.code;

import cn.edu.fudan.se.multidependency.model.node.Node;

public interface CodeUnit extends Node, NodeWithLine {
	
	String getIdentifier();
	
	void setIdentifier(String identifier);
	
	String getIdentifierSimpleName();

    String getIdentifierSuffix();
	
}
