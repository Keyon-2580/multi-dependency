package cn.edu.fudan.se.multidependency.model.relation.clone;

import cn.edu.fudan.se.multidependency.model.relation.Relation;

public interface CloneRelation extends Relation {

	double getValue();
	
	int getNode1StartLine();
	
	int getNode1EndLine();
	
	int getNode2StartLine();
	
	int getNode2EndLine();
	
	int getNode1Index();

	int getNode2Index();
}
