package cn.edu.fudan.se.multidependency.service.spring.clone.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Getter;
import lombok.Setter;

public class PackageCloneValueCalculator implements CloneValueCalculator<Boolean> {
	
	private PackageCloneValueCalculator() {}
	
	private static PackageCloneValueCalculator instance = new PackageCloneValueCalculator();
	
	/**
	 * 若两个包内的有克隆关系的文件总数大于等于此值，则认为这两个包之间重复读过高
	 */
	@Setter
	@Getter
	private int countThreshold;
	
	@Setter
	@Getter
	private double percentageThreshold;
	
	public static PackageCloneValueCalculator getInstance() {
		return instance;
	}
	
	@Override
	public Boolean calculate(CloneValueForDoubleNodes<? extends Node> clone) {
		Node node1 = clone.getNode1();
		Node node2 = clone.getNode2();
		if(!(node1 instanceof Package) || !(node2 instanceof Package)) {
			return false;
		}
		if(clone.getNodesInNode1().size() + clone.getNodesInNode2().size() >= countThreshold) {
			return true;
		}
		try {
			if((clone.getNodesInNode1().size() + clone.getNodesInNode2().size() + 0.0) 
					/ (clone.getAllNodesInNode1().size() + clone.getAllNodesInNode2().size()) >= percentageThreshold) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		
		return false;
	}

}
