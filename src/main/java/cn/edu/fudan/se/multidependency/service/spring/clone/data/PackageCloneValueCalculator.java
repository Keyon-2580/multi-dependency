package cn.edu.fudan.se.multidependency.service.spring.clone.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.service.spring.ContainRelationService;

public class PackageCloneValueCalculator implements CloneValueCalculator<Boolean> {
	
	private PackageCloneValueCalculator() {}
	
	private static PackageCloneValueCalculator instance = new PackageCloneValueCalculator();
	
	private ContainRelationService containRelationService;
	
	/**
	 * 最小克隆相关文件数
	 * 若两个包内的有克隆关系的文件总数大于等于此值，则认为这两个包之间重复读过高
	 */
	private int threshold;
	
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
//		Package pck1 = (Package) node1;
//		Package pck2 = (Package) node2;
//		Collection<ProjectFile> files1 = containRelationService.findPackageContainFiles(pck1);
//		Collection<ProjectFile> files2 = containRelationService.findPackageContainFiles(pck2);
//		List<Clone> clones = clone.getChildren();
		if(clone.getNodesInNode1().size() + clone.getNodesInNode2().size() >= threshold) {
			return true;
		}
		
		return false;
	}

	public ContainRelationService getContainRelationService() {
		return containRelationService;
	}

	public void setContainRelationService(ContainRelationService containRelationService) {
		this.containRelationService = containRelationService;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

}