package cn.edu.fudan.se.multidependency.service.spring.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.clone.HasCloneValueRelation;
import lombok.Data;

/**
 * 节点node1和node2不分方向
 * @author fan
 *
 */
@Data
public class Clone<N extends Node, C extends HasCloneValueRelation> implements Serializable {
	
	private static final long serialVersionUID = -2262794801616872866L;
	
	private N node1;
	
	private N node2;
	
	private double value = 0;
	
	public String getId() {
		return node1.getId() + "_" + node2.getId();
	}
	
	// 两个克隆节点内部的方法克隆对
	private Collection<C> children = new ArrayList<>();
	
	public int sizeOfChildren() {
		return children.size();
	}
	
	public static interface CloneValueCalculator {
		String calculate(Clone<? extends Node, ? extends HasCloneValueRelation> clone);
	}
	
	private transient CloneValueCalculator calculator;
	
	public String calculateValue() {
		if(calculator != null) {
			return this.calculator.calculate(this);
		}
		return "clone: " + getValue();
	}
	
	public void addChild(C clone) {
		this.children.add(clone);
		this.value += clone.getValue();
	}
	
	public void addChildren(Collection<C> clones) {
		for(C clone : clones) {
			addChild(clone);
		}
	}
	
}
