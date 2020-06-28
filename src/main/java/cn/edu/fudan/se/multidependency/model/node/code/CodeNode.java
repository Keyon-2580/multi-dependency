package cn.edu.fudan.se.multidependency.model.node.code;

import cn.edu.fudan.se.multidependency.model.node.Node;

public interface CodeNode extends Node {
	
	String getIdentifier();
	
	void setIdentifier(String identifier);
	
	String getIdentifierSimpleName();

    String getIdentifierSuffix();

	int getStartLine();
	
	int getEndLine();
	
	default int getLines() {
		if(getStartLine() <= 0 || getEndLine() <= 0) {
			return -1;
		}
		return getEndLine() - getStartLine() + 1;
	}
	
}
