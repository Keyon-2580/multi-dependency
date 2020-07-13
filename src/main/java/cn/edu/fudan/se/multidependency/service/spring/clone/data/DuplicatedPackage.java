package cn.edu.fudan.se.multidependency.service.spring.clone.data;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Package;
import lombok.Data;
import lombok.NonNull;

@Data
public class DuplicatedPackage {

	private CloneValueForDoubleNodes<Package> clonePackages;
	
	private Map<String, DuplicatedPackage> childrenClonePackages;
	
	private Package package1;
	
	private Package package2;
	
	private double value;
	
	private void swapPackage() {
		Package pck = package1;
		package1 = package2;
		package2 = pck;
	}
	
	public DuplicatedPackage(@NonNull CloneValueForDoubleNodes<Package> clonePackages) {
		this.clonePackages = clonePackages;
		this.childrenClonePackages = new HashMap<>();
		this.package1 = clonePackages.getNode1();
		this.package2 = clonePackages.getNode2();
		if(package1.getDirectoryPath().compareTo(package2.getDirectoryPath()) > 0) {
			swapPackage();
		}
	}
	
	public void addChild(DuplicatedPackage child) {
		if(childrenClonePackages == null) {
			return ;
		}
		this.childrenClonePackages.put(child.getId(), child);
	}

	public CloneValueForDoubleNodes<Package> getClonePackages() {
		return clonePackages;
	}
	
	public String getId() {
		return clonePackages.getId();
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
