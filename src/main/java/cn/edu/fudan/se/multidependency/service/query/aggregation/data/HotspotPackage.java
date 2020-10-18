package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
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
	private int clonePairs;

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

	@Getter
	@Setter
	private int packageCochangeTimes = 0;

	@Getter
	@Setter
	private int packageCloneCochangeTimes = 0;
	
	public HotspotPackage(@NonNull RelationDataForDoubleNodes<Node, Relation> relationPackages) {
		this.relationPackages = relationPackages;
		this.childrenHotspotPackages = new ArrayList<>();
		this.childrenOtherPackages1 = new ArrayList<>();
		this.childrenOtherPackages2 = new ArrayList<>();
		this.package1 = (Package) relationPackages.getNode1();
		this.package2 = (Package) relationPackages.getNode2();
		this.id = relationPackages.getId();
		this.clonePairs = relationPackages.getChildren().size();
		this.allNodes1 = 0;
		this.allNodes2 = 0;
		this.relationNodes1 = 0;
		this.relationNodes2 = 0;
	}

	public boolean addHotspotChild(HotspotPackage child) {
		if(childrenHotspotPackages == null) {
			return false;
		}
		if(!this.childrenHotspotPackages.contains(child)) {
			this.childrenHotspotPackages.add(child);
		}
		return true;
	}

	public boolean addOtherChild1(Package child) {
		if(childrenOtherPackages1 == null) {
			return false;
		}
		if(!this.childrenOtherPackages1.contains(child)) {
			this.childrenOtherPackages1.add(child);
		}
		return true;
	}

	public boolean addOtherChild2(Package child) {
		if(childrenOtherPackages2 == null) {
			return false;
		}
		if(!this.childrenOtherPackages2.contains(child)) {
			this.childrenOtherPackages2.add(child);
		}
		return true;
	}

	public void setData(int allNodes1, int allNodes2, int relationNodes1, int relationNodes2) {
		this.allNodes1 = allNodes1;
		this.allNodes2 = allNodes2;
		this.relationNodes1 = relationNodes1;
		this.relationNodes2 = relationNodes2;
	}

	public void setClonePairs(int clonePairs) {
		this.clonePairs = clonePairs;
	}
	public void swapPackages() {
		Package pck = this.package1;
		this.package1 = this.package2;
		this.package2 = pck;
	}
}
