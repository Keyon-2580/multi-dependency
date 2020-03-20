package cn.edu.fudan.se.multidependency.model.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCallFunction;

public class Relations {

	private Map<RelationType, List<Relation>> allRelations = new HashMap<>();
	
	private Map<String, List<DynamicCallFunction>> traceIdToDynamicCallFunctions = new HashMap<>();
	
	public void clear() {
		allRelations.clear();
		traceIdToDynamicCallFunctions.clear();
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
	
	public void addRelation(Relation relation) {
		List<Relation> nodes = allRelations.get(relation.getRelationType());
		nodes = nodes == null ? new ArrayList<>() : nodes;
		nodes.add(relation);
		allRelations.put(relation.getRelationType(), nodes);
		
		if(relation instanceof DynamicCallFunction) {
			DynamicCallFunction call = (DynamicCallFunction) relation;
			if(call.getTraceId() == null) {
				return;
			}
			List<DynamicCallFunction> calls = traceIdToDynamicCallFunctions.get(call.getTraceId());
			calls = calls == null ? new ArrayList<>() : calls;
			calls.add(call);
			traceIdToDynamicCallFunctions.put(call.getTraceId(), calls);
		}
	}
	
	public List<DynamicCallFunction> findDynamicCallFunctionsByTraceId(String traceId) {
		try {
			List<DynamicCallFunction> calls = traceIdToDynamicCallFunctions.get(traceId);
			return calls == null ? new ArrayList<>() : calls;
		} catch (Exception e) {
		}
		return new ArrayList<>();
	}
	
	public List<? extends Relation> findRelationsMap(RelationType relationType) {
		List<? extends Relation> relations = allRelations.get(relationType);
		return relations == null ? new ArrayList<>() : relations;
	}
	
	public boolean existRelation(Relation relation) {
		List<Relation> relations = this.allRelations.get(relation.getRelationType());
		if(relations == null) {
			return false;
		}
		return relations.contains(relation);
	}
	
}
