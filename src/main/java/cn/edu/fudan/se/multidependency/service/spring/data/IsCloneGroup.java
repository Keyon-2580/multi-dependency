package cn.edu.fudan.se.multidependency.service.spring.data;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneRelationNode;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelation;

public interface IsCloneGroup {
	
	CloneGroup getGroup();
	
	Collection<? extends CloneRelationNode> getNodes();
	
	Collection<? extends CloneRelation> getRelations();
	
	default int sizeOfNodes() {
		return getNodes().size();
	}
	
	default int sizeOfRelations() {
		return getRelations().size();
	}
	
}
