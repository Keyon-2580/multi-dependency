package cn.edu.fudan.se.multidependency.model.relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import lombok.Data;

/**
 * 节点node1和node2不分顺序
 * @author fan
 *
 */
@Data
public class Clone implements Serializable {
	
	private static final long serialVersionUID = -2262794801616872866L;
	
	private NodeLabelType level;

	private Node node1;
	
	private Node node2;
	
	private double value = 0;
	
	private List<Clone> children = new ArrayList<>();
	
	public static interface CloneValueCalculator {
		String calculate(Clone clone);
	}
	
	private CloneValueCalculator calculator;
	
	public String calculateValue() {
		if(calculator != null) {
			return this.calculator.calculate(this);
		}
		return "clone: " + getValue();
	}
	
	public void addChild(Clone clone) {
		if(this.equals(clone)) {
			return ;
		}
		this.children.add(clone);
		this.value += clone.getValue();
	}
	
	public void addChildren(Collection<Clone> clones) {
		for(Clone clone : clones) {
			addChild(clone);
		}
	}
	
	public static Clone changeFunctionCloneToClone(FunctionCloneFunction functionCloneFunction) {
		Clone result = new Clone();
		result.setNode1(functionCloneFunction.getFunction1());
		result.setNode2(functionCloneFunction.getFunction2());
		result.setValue(functionCloneFunction.getValue());
		result.setLevel(NodeLabelType.Function);
		return result;
	}
	
	public boolean isSameClone(Clone other) {
		return (node1.equals(other.getNode1()) && node2.equals(other.getNode2())) 
				|| (node2.equals(other.getNode1()) && node1.equals(other.getNode2()));
	}
	
}
