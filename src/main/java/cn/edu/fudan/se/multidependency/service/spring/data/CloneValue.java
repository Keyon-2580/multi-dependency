package cn.edu.fudan.se.multidependency.service.spring.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
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
	
	// 两个克隆节点内部的克隆关系
	private List<Clone> children = new ArrayList<>();
	
	public int sizeOfChildren() {
		return children.size();
	}
	
	public void sortChildren() {
		children.sort((clone1, clone2) -> {
			CodeNode node11 = clone1.getCodeNode1();
			CodeNode node21 = clone2.getCodeNode1();
			int sort = node11.getIdentifier().compareTo(node21.getIdentifier());
			if(sort == 0) {
				CodeNode node12 = clone1.getCodeNode2();
				CodeNode node22 = clone2.getCodeNode2();
				return node12.getIdentifier().compareTo(node22.getIdentifier());
			} else {
				return sort;
			}
		});
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
		addValue(clone.getValue());
	}
	
	public void addValue(double value) {
		this.value += value;
	}
	
	public void addChildren(Collection<Clone> clones) {
		for(Clone clone : clones) {
			addChild(clone);
		}
	}
	
}
