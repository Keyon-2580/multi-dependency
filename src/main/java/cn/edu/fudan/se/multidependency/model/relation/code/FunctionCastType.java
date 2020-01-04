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

@RelationshipEntity(RelationType.str_FUNCTION_CAST_TYPE)
public class FunctionCastType implements Relation {

	private static final long serialVersionUID = -7384669294027502528L;

	@Id
    @GeneratedValue
    private Long id;
	
	private Function function;
	
	private Type castType;

	public FunctionCastType() {
		super();
	}

	public FunctionCastType(Function function, Type castType) {
		super();
		this.function = function;
		this.castType = castType;
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
		return castType.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_CAST_TYPE;
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

	public Type getCastType() {
		return castType;
	}

	public void setCastType(Type castType) {
		this.castType = castType;
	}

}
