package cn.edu.fudan.se.multidependency.model.node.code;

public interface NodeWithLine {

	int getStartLine();
	
	int getEndLine();
	
	default int getLines() {
		if(getStartLine() <= 0 || getEndLine() <= 0) {
			return -1;
		}
		return getEndLine() - getStartLine() + 1;
	}
	
}
