package fan.md.model.node.dynamic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fan.md.model.node.code.Function;

public class CallNode implements Serializable {
	
	private static final long serialVersionUID = 8437183814030592952L;

	private CallNode father;

	private List<CallNode> children = new ArrayList<>();
	
	private Function currentNode;
	
	public CallNode(Function function) {
		this.father = null;
		this.currentNode = function;
	}
	
	public CallNode(CallNode father, Function function) {
		this.father = father;
		this.currentNode = function;
	}

	public CallNode getFather() {
		return father;
	}

	public void setFather(CallNode father) {
		this.father = father;
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
	
}
