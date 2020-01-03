package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_FUNCTION_ANNOTATION_TYPE)
public class FunctionAnnotationType implements Relation {

	private static final long serialVersionUID = 8248026322068428052L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public Long getId() {
		return id;
	}

	public FunctionAnnotationType() {
		super();
	}

	public FunctionAnnotationType(Function function, Type type) {
		super();
		this.function = function;
		this.type = type;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	private Function function;
	private Type type;

	@Override
	public Long getStartNodeGraphId() {
		return function.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return type.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_ANNOTATION_TYPE;
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

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
