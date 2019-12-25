package cn.edu.fudan.se.multidependency.model.node.dynamic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.code.Function;

public class CallNode implements Serializable {
	
	private static final long serialVersionUID = 8437183814030592952L;

	private CallNode parent;

	private List<CallNode> children = new ArrayList<>();
	
	private Function currentNode;
	
	public CallNode(Function function) {
		this.setParent(null);
		this.currentNode = function;
	}
	
	public CallNode(CallNode father, Function function) {
		this.setParent(father);
		this.currentNode = function;
	}

	public List<CallNode> getChildren() {
		return children;
	}
	
	public void addChild(CallNode child) {
		this.children.add(child);
	}

	public Function getFunction() {
		return currentNode;
	}

	public void setFunction(Function function) {
		this.currentNode = function;
	}

	public CallNode getParent() {
		return parent;
	}

	public void setParent(CallNode parent) {
		this.parent = parent;
	}
	
}
