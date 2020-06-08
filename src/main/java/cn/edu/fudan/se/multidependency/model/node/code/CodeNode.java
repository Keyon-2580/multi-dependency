package cn.edu.fudan.se.multidependency.model.node.code;

import cn.edu.fudan.se.multidependency.model.node.Node;

public interface CodeNode extends Node {
	
	String getIdentifier();
	
	void setIdentifier(String identifier);
	
	String getIdentifierSimpleName();

    String getIdentifierSuffix();
    
    static final String FILE_IDENTIFIER_SUFFIX = "#F";
	
}
