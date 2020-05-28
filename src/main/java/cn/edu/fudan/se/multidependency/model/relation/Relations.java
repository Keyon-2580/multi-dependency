package cn.edu.fudan.se.multidependency.model.relation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCallFunction;

public class Relations {

	private Map<RelationType, List<Relation>> allRelations = new ConcurrentHashMap<>();
	
	private Map<String, List<DynamicCallFunction>> traceIdToDynamicCallFunctions = new ConcurrentHashMap<>();
	
	private Map<Node, Map<Node, Map<RelationType, RelationWithTimes>>> startNodesToNodeRelations = new ConcurrentHashMap<>();
	
	private RelationWithTimes hasRelationWithTimes(Node startNode, Node endNode, RelationType relationType) {
		Map<Node, Map<RelationType, RelationWithTimes>> endNodesTemp = startNodesToNodeRelations.get(startNode);
		if(endNodesTemp == null) {
			return null;
		}
		Map<RelationType, RelationWithTimes> relationsTemp = endNodesTemp.get(endNode);
		if(relationsTemp == null) {
			return null;
		}
		return relationsTemp.get(relationType);
	}
	
	private void addRelationWithTimes(RelationWithTimes relation) {
		Map<Node, Map<RelationType, RelationWithTimes>> endNodesTemp = startNodesToNodeRelations.getOrDefault(relation.getStartNode(), new ConcurrentHashMap<>());
		Map<RelationType, RelationWithTimes> relationsTemp = endNodesTemp.getOrDefault(relation.getEndNode(), new ConcurrentHashMap<>());
		relationsTemp.put(relation.getRelationType(), relation);
		endNodesTemp.put(relation.getEndNode(), relationsTemp);
		startNodesToNodeRelations.put(relation.getStartNode(), endNodesTemp);
	}
	
	public void clear() {
		allRelations.clear();
		traceIdToDynamicCallFunctions.clear();
		startNodesToNodeRelations.clear();
	}
	
	public Map<RelationType, List<Relation>> getAllRelations() {
		return new ConcurrentHashMap<>(allRelations);
	}
	
	public int size() {
		int size = 0;
		for(List<Relation> nodes : allRelations.values()) {
			size += nodes.size();
		}
		return size;
	}
	
	public synchronized void addRelation(Relation relation) {
		
		if(relation instanceof DynamicCallFunction) {
			DynamicCallFunction call = (DynamicCallFunction) relation;
			if(call.getTraceId() == null) {
				return;
			}
			List<DynamicCallFunction> calls = traceIdToDynamicCallFunctions.getOrDefault(call.getTraceId(), new CopyOnWriteArrayList<>());
			calls.add(call);
			traceIdToDynamicCallFunctions.put(call.getTraceId(), calls);
		}
		
		if(relation instanceof RelationWithTimes) {
			RelationWithTimes relationWithTimes = hasRelationWithTimes(relation.getStartNode(), relation.getEndNode(), relation.getRelationType());
			if(relationWithTimes == null) {
				addRelationWithTimes((RelationWithTimes) relation);
				addRelationDirectly(relation);
			} else {
				relationWithTimes.addTimes();
			}
		} else {
			addRelationDirectly(relation);
		}
		
	}
	
	private void addRelationDirectly(Relation relation) {
		List<Relation> relations = allRelations.getOrDefault(relation.getRelationType(), new CopyOnWriteArrayList<>());
		relations.add(relation);
		allRelations.put(relation.getRelationType(), relations);
	}
	
	public List<DynamicCallFunction> findDynamicCallFunctionsByTraceId(String traceId) {
		return traceIdToDynamicCallFunctions.getOrDefault(traceId, new CopyOnWriteArrayList<>());
	}
	
	public List<? extends Relation> findRelationsMap(RelationType relationType) {
		return allRelations.getOrDefault(relationType, new CopyOnWriteArrayList<>());
	}
	
	public boolean existRelation(Relation relation) {
		List<Relation> relations = this.allRelations.get(relation.getRelationType());
		return relations == null ? false : relations.contains(relation);
	}
	
}
