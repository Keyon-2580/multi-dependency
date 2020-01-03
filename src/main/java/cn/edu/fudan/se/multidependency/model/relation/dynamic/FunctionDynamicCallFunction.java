package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION)
public class FunctionDynamicCallFunction implements Relation {

	private static final long serialVersionUID = -7640490954063715746L;
	
	@StartNode
	private Function function;
	
	@EndNode
	private Function callFunction;
	
	public FunctionDynamicCallFunction() {
		super();
	}

	public FunctionDynamicCallFunction(Function function, Function callFunction) {
		super();
		this.function = function;
		this.callFunction = callFunction;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	private String order;
	
//	private String testCaseName;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getStartNodeGraphId() {
		return function.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return callFunction.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.DYNAMIC_FUNCTION_CALL_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("order", getOrder() == null ? "" : getOrder());
//		properties.put("testCaseName", getTestCaseName() == null ? "" : getTestCaseName());
		return properties;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public Function getCallFunction() {
		return callFunction;
	}

	public void setCallFunction(Function callFunction) {
		this.callFunction = callFunction;
	}

//	public String getTestCaseName() {
//		return testCaseName;
//	}
//
//	public void setTestCaseName(String testCaseName) {
//		this.testCaseName = testCaseName;
//	}
	
}
