package cn.edu.fudan.se.multidependency.service.query.clone.data;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class SimilarPackage {

	@Getter
	private CloneValueForDoubleNodes<Package> clonePackages;
	
	@Getter
	private Map<String, SimilarPackage> childrenClonePackages;
	
	@Getter
	private Package package1;
	
	@Getter
	private Package package2;
	
	@Getter
	private String id;
	
	@Getter
	@Setter
	private double value;
	
	private void swapPackage() {
		Package pck = package1;
		package1 = package2;
		package2 = pck;
	}
	
	public SimilarPackage(@NonNull CloneValueForDoubleNodes<Package> clonePackages) {
		this.clonePackages = clonePackages;
		this.childrenClonePackages = new HashMap<>();
		this.package1 = clonePackages.getNode1();
		this.package2 = clonePackages.getNode2();
		if(package1.getDirectoryPath().compareTo(package2.getDirectoryPath()) > 0) {
			swapPackage();
		}
		this.id = clonePackages.getId();
	}
	
	public void addChild(SimilarPackage child) {
		if(childrenClonePackages == null) {
			return ;
		}
		this.childrenClonePackages.put(child.getId(), child);
	}

}
