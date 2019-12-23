package fan.md.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import fan.md.model.node.code.Function;
import fan.md.model.node.code.Type;
import fan.md.model.relation.Relation;
import fan.md.model.relation.RelationType;

@RelationshipEntity("FUNCTION_RETURN_TYPE")
public class FunctionReturnType implements Relation {

	private static final long serialVersionUID = -3315100529955945595L;

	private Function function;
	
	private Type returnType;
	
	public FunctionReturnType() {
		super();
	}

	public FunctionReturnType(Function function, Type returnType) {
		super();
		this.function = function;
		this.returnType = returnType;
	}

	@Id
    @GeneratedValue
    private Long id;
	
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
		return returnType.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_RETURN_TYPE;
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

	public Type getReturnType() {
		return returnType;
	}

	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}

}
