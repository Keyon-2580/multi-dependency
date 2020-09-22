package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.service.query.clone.data.SimilarPackage;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

public class HotspotPackage {

	@Getter
	private RelationDataForDoubleNodes<Node, Relation> relationPackages;
	
	@Getter
	private Collection<HotspotPackage> childrenHotspotPackages;

	@Getter
	private Collection<Package> childrenOtherPackages1;

	@Getter
	private Collection<Package> childrenOtherPackages2;
	
	@Getter
	private Package package1;
	
	@Getter
	private Package package2;

	@Getter
	private int relationNodes1;

	@Getter
	private int relationNodes2;

	@Getter
	private int allNodes1;

	@Getter
	private int allNodes2;
	
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
	
	public HotspotPackage(@NonNull RelationDataForDoubleNodes<Node, Relation> relationPackages) {
		this.relationPackages = relationPackages;
		this.childrenHotspotPackages = new ArrayList<>();
		this.childrenOtherPackages1 = new ArrayList<>();
		this.childrenOtherPackages2 = new ArrayList<>();
		this.package1 = (Package) relationPackages.getNode1();
		this.package2 = (Package) relationPackages.getNode2();
//		if(package1.getDirectoryPath().compareTo(package2.getDirectoryPath()) > 0) {
//			swapPackage();
//		}
		this.id = relationPackages.getId();
	}

	public void addHotspotChild(HotspotPackage child) {
		if(childrenHotspotPackages == null) {
			return ;
		}
		this.childrenHotspotPackages.add(child);
	}

	public void addOtherChild1(Package child) {
		if(childrenOtherPackages1 == null) {
			return ;
		}
		this.childrenOtherPackages1.add(child);
	}

	public void addOtherChild2(Package child) {
		if(childrenOtherPackages2 == null) {
			return ;
		}
		this.childrenOtherPackages2.add(child);
	}

	public void setData(int allNodes1, int allNodes2, int relationNodes1, int relationNodes2) {
		this.allNodes1 = allNodes1;
		this.allNodes2 = allNodes2;
		this.relationNodes1 = relationNodes1;
		this.relationNodes2 = relationNodes2;
	}

	public boolean isContainHotspotChild(HotspotPackage p) {
		return this.childrenHotspotPackages.contains(p);
	}

	public boolean isContainOtherChild1(Package p) {
		return this.childrenOtherPackages1.contains(p);
	}

	public boolean isContainOtherChild2(Package p) {
		return this.childrenOtherPackages2.contains(p);
	}

}
