package cn.edu.fudan.se.multidependency.service.query.clone.data;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Getter;
import lombok.Setter;

/**
 * 默认的判定有克隆关系的两个包是否相似的类
 * @author fan
 *
 */
public class PackageCloneValueCalculatorByFileLoc implements CloneValueCalculator<Boolean> {
	
	public static final double DEFAULT_PERCENTAGE_THRESHOLD = 0.5;
	
	private PackageCloneValueCalculatorByFileLoc() {
		initThreshold();
	}
	
	private static PackageCloneValueCalculatorByFileLoc instance = new PackageCloneValueCalculatorByFileLoc();
	
	@Setter
	@Getter
	private double percentageThreshold = DEFAULT_PERCENTAGE_THRESHOLD;
	
	public static PackageCloneValueCalculatorByFileLoc getInstance() {
		return instance;
	}
	
	public void initThreshold() {
		this.percentageThreshold = DEFAULT_PERCENTAGE_THRESHOLD;
	}

	@Override
	public Boolean calculate(CloneValueForDoubleNodes<? extends Node> clone) {
		Node node1 = clone.getNode1();
		Node node2 = clone.getNode2();
		if(!(node1 instanceof Package) || !(node2 instanceof Package)) {
			return false;
		}
		try {
			Collection<CodeNode> nodesInPackage1 = clone.getNodesInNode1();
			Collection<CodeNode> nodesInPackage2 = clone.getNodesInNode2();
			
			long cloneFilesLOC = 0;
			for(CodeNode node : nodesInPackage1) {
				if(!(node instanceof ProjectFile)) {
					throw new Exception("克隆节点不为file类型");
				}
				cloneFilesLOC += ((ProjectFile) node).getLoc();
			}
			for(CodeNode node : nodesInPackage2) {
				if(!(node instanceof ProjectFile)) {
					throw new Exception("克隆节点不为file类型");
				}
				cloneFilesLOC += ((ProjectFile) node).getLoc();
			}
			
			long allPackageLoc = ((Package) node1).getLoc() + ((Package) node2).getLoc();
			
			if((cloneFilesLOC + 0.0) / (allPackageLoc) >= percentageThreshold) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		
		return false;
	}

}
