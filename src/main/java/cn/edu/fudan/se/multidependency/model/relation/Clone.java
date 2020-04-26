package cn.edu.fudan.se.multidependency.model.relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;
import lombok.Getter;
import lombok.Setter;

/**
 * 节点node1和node2不分顺序
 * @author fan
 *
 */
public class Clone implements Serializable {
	
	private static final long serialVersionUID = -2262794801616872866L;

	@Getter
	@Setter
	private Node node1;
	
	@Getter
	@Setter
	private Node node2;
	
	@Getter
	@Setter
	private double value = 0;
	
	private List<Clone> children = new ArrayList<>();
	
	public void addChild(Clone clone) {
		if(this.equals(clone)) {
			return ;
		}
		this.children.add(clone);
		this.value += clone.getValue();
	}
	
	public static Clone changeFunctionCloneToClone(FunctionCloneFunction functionCloneFunction) {
		Clone result = new Clone();
		result.setNode1(functionCloneFunction.getFunction1());
		result.setNode2(functionCloneFunction.getFunction2());
		result.setValue(functionCloneFunction.getValue());
		return result;
	}
	
	public boolean isSameClone(Clone other) {
		return (node1.equals(other.getNode1()) && node2.equals(other.getNode2())) 
				|| (node2.equals(other.getNode1()) && node1.equals(other.getNode2()));
	}
	
}
