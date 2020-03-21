package cn.edu.fudan.se.multidependency.model.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.DynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;

public class Relations {

	private Map<RelationType, List<Relation>> allRelations = new HashMap<>();
	
	private Map<String, List<DynamicCallFunction>> traceIdToDynamicCallFunctions = new HashMap<>();
	
	private Map<Function, Map<Function, FunctionCallFunction>> functionCallFunctions = new HashMap<>();
	
	public void clear() {
		allRelations.clear();
		traceIdToDynamicCallFunctions.clear();
		functionCallFunctions.clear();
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
		
		if(relation instanceof DynamicCallFunction) {
			DynamicCallFunction call = (DynamicCallFunction) relation;
			if(call.getTraceId() == null) {
				return;
			}
			List<DynamicCallFunction> calls = traceIdToDynamicCallFunctions.getOrDefault(call.getTraceId(), new ArrayList<>());
			calls.add(call);
			traceIdToDynamicCallFunctions.put(call.getTraceId(), calls);
		}
		
		if(relation instanceof FunctionCallFunction) {
			FunctionCallFunction temp = (FunctionCallFunction) relation;
			Function caller = temp.getFunction();
			Function called = temp.getCallFunction();
			FunctionCallFunction hasCall = hasFunctionCallFunction(caller, called);
			if(hasCall != null) {
				hasCall.addTimes();
				return;
			} else {
				Map<Function, FunctionCallFunction> tempCall = this.functionCallFunctions.getOrDefault(caller, new HashMap<>());
				tempCall.put(called, temp);
				this.functionCallFunctions.put(caller, tempCall);
			}
		}
		
		List<Relation> nodes = allRelations.getOrDefault(relation.getRelationType(), new ArrayList<>());
		nodes.add(relation);
		allRelations.put(relation.getRelationType(), nodes);
	}
	
	public FunctionCallFunction hasFunctionCallFunction(Function caller, Function called) {
		Map<Function, FunctionCallFunction> calls = this.functionCallFunctions.get(caller);
		return calls == null ? null : calls.get(called);
	}
	
	public List<DynamicCallFunction> findDynamicCallFunctionsByTraceId(String traceId) {
		return traceIdToDynamicCallFunctions.getOrDefault(traceId, new ArrayList<>());
	}
	
	public List<? extends Relation> findRelationsMap(RelationType relationType) {
		return allRelations.getOrDefault(relationType, new ArrayList<>());
	}
	
	public boolean existRelation(Relation relation) {
		List<Relation> relations = this.allRelations.get(relation.getRelationType());
		return relations == null ? false : relations.contains(relation);
	}
	
}
