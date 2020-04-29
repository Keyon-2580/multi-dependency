package cn.edu.fudan.se.multidependency.model.relation.clone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;

/**
 * 节点node1和node2不分顺序
 * @author fan
 *
 */
@Data
public class Clone<T extends Node> implements Serializable {
	
	private static final long serialVersionUID = -2262794801616872866L;
	
	private T node1;
	
	private T node2;
	
	private double value = 0;
	
	public String getId() {
		return node1.getId() + "_" + node2.getId();
	}
	
	// 两个克隆节点内部的方法克隆对
	private List<FunctionCloneFunction> children = new ArrayList<>();
	
	public int sizeOfChildren() {
		return children.size();
	}
	
	public static interface CloneValueCalculator {
		String calculate(Clone<? extends Node> clone);
	}
	
	private transient CloneValueCalculator calculator;
	
	public String calculateValue() {
		if(calculator != null) {
			return this.calculator.calculate(this);
		}
		return "clone: " + getValue();
	}
	
	public void addChild(FunctionCloneFunction clone) {
		this.children.add(clone);
		this.value += clone.getValue();
	}
	
	public void addChildren(Collection<FunctionCloneFunction> clones) {
		for(FunctionCloneFunction clone : clones) {
			addChild(clone);
		}
	}
	
}
