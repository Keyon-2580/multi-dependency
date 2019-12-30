package cn.edu.fudan.se.multidependency.model.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Relations {

	private Map<RelationType, List<Relation>> allRelations = new HashMap<>();
	
	public void clear() {
		allRelations.clear();
	}
	
	public Map<RelationType, List<Relation>> getAllRelations() {
		return new HashMap<>(allRelations);
	}
	
	public int size() {
		int size = 0;
		for(List<Relation> nodes : allRelations.values()) {
			size += nodes.size();
		}
		return size;
	}
	
	public void clear(RelationType relationType) {
		List<Relation> nodes = allRelations.get(relationType);
		if(nodes != null) {
			nodes.clear();
		}
	}
	
	public void addRelation(Relation relation) {
		List<Relation> nodes = allRelations.get(relation.getRelationType());
		nodes = nodes == null ? new ArrayList<>() : nodes;
		nodes.add(relation);
		allRelations.put(relation.getRelationType(), nodes);
	}
	
	public List<? extends Relation> findRelationsMap(RelationType relationType) {
		List<? extends Relation> relations = allRelations.get(relationType);
		return relations == null ? new ArrayList<>() : relations;
	}
}
