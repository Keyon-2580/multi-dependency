package fan.md.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import fan.md.model.node.code.Function;
import fan.md.model.relation.Relation;
import fan.md.model.relation.RelationType;

@RelationshipEntity("FUNCTION_CALL_FUNCTION")
public class FunctionCallFunction implements Relation {
	
	private static final long serialVersionUID = 5982413005555063698L;

	@StartNode
	private Function function;
	
	@EndNode
	private Function callFunction;
	
	@Id
    @GeneratedValue
    private Long id;
	

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
		return RelationType.FUNCTION_CALL_FUNCTION;
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
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}
}
