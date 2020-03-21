package cn.edu.fudan.se.multidependency.model.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelationTimes {

	private Map<RelationType, List<Relation>> relations = new HashMap<>();
	
	private int times = 0;
	
	
	public void addRelation(Relation relation) {
		List<Relation> relations = this.relations.getOrDefault(relation.getRelationType(), new ArrayList<>());
		relations.add(relation);
		this.relations.put(relation.getRelationType(), relations);
	}
	
	public void addTimes() {
		times++;
	}
}
