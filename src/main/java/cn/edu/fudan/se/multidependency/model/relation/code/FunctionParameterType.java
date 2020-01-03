package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.code.Type;

@RelationshipEntity(RelationType.str_FUNCTION_PARAMETER_TYPE)
public class FunctionParameterType implements Relation {

	private static final long serialVersionUID = -8796616144049338126L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	private Function function;
	
	private Type parameterType;

	public FunctionParameterType() {
		super();
	}

	public FunctionParameterType(Function function, Type parameterType) {
		super();
		this.function = function;
		this.parameterType = parameterType;
	}

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
		return parameterType.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_PARAMETER_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public Type getParameterType() {
		return parameterType;
	}

	public void setParameterType(Type parameterType) {
		this.parameterType = parameterType;
	}

}
