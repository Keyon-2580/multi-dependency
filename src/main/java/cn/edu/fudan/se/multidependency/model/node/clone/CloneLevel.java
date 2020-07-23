package cn.edu.fudan.se.multidependency.model.node.clone;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.node.code.Type;

public enum CloneLevel {
	file, function, type, snippet, multiple_level;
	
	public static CloneLevel getCodeNodeCloneLevel(CodeNode node) {
		if(node instanceof ProjectFile) {
			return file;
		} else if(node instanceof Type) {
			return function;
		} else if(node instanceof Function) {
			return type;
		} else if(node instanceof Snippet) {
			return snippet;
		}
		return null;
	}
}
