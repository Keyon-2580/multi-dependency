package cn.edu.fudan.se.multidependency.service.query.aggregation.data;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class HotspotPackagePair {

	private String id;

	private RelationType hotspotRelationType;

	private BasicDataForDoubleNodes<Node, Relation> packagePairRelationData;

	private List<HotspotPackagePair> childrenHotspotPackagePairs;

	private List<Package> childrenOtherPackages1;

	private List<Package> childrenOtherPackages2;

	private Package package1;
	
	private Package package2;

	private int relationNodes1;

	private int relationNodes2;

	private int allNodes1;

	private int allNodes2;

	public HotspotPackagePair(@NonNull Package package1, Package package2, BasicDataForDoubleNodes<Node, Relation> packagePairRelationData) {
		this.packagePairRelationData = packagePairRelationData;
		this.hotspotRelationType = packagePairRelationData.getRelationDataType();
		this.childrenHotspotPackagePairs = new ArrayList<>();
		this.childrenOtherPackages1 = new ArrayList<>();
		this.childrenOtherPackages2 = new ArrayList<>();
		this.package1 = package1;
		this.package2 = package2;
		this.id = String.join("_", package1.getId().toString(), package2.getId().toString());
	}

	public boolean addHotspotChild(HotspotPackagePair child) {
		if(childrenHotspotPackagePairs == null) {
			return false;
		}
		if(!this.childrenHotspotPackagePairs.contains(child)) {
			this.childrenHotspotPackagePairs.add(child);
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

	public void swapPackages() {
		Package pck = this.package1;
		this.package1 = this.package2;
		this.package2 = pck;
	}
}
