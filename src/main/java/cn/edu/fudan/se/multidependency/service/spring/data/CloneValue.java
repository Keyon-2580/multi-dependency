package cn.edu.fudan.se.multidependency.service.spring.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import lombok.Data;

/**
 * 节点node1和node2不分方向
 * @author fan
 *
 */
@Data
public class CloneValue<N extends Node> implements Serializable {
	
	private static final long serialVersionUID = -2262794801616872866L;
	
	private N node1;
	
	private N node2;
	
	private double value = 0;
	
	public String getId() {
		return node1.getId() + "_" + node2.getId();
	}
	
	// 两个克隆节点内部的克隆对
	private Collection<Clone> children = new ArrayList<>();
	
	public int sizeOfChildren() {
		return children.size();
	}
	
	public static interface CloneValueCalculator {
		String calculate(CloneValue<? extends Node> clone);
	}
	
	private transient CloneValueCalculator calculator;
	
	public String calculateValue() {
		if(calculator != null) {
			return this.calculator.calculate(this);
		}
		return "clone: " + getValue();
	}
	
	public void addChild(Clone clone) {
		this.children.add(clone);
		this.value += clone.getValue();
	}
	
	public void addChildren(Collection<Clone> clones) {
		for(Clone clone : clones) {
			addChild(clone);
		}
	}
	
}