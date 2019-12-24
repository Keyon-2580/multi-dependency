package fan.md.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import fan.md.model.node.code.Function;
import fan.md.model.node.code.Variable;
import fan.md.model.relation.Relation;
import fan.md.model.relation.RelationType;

@RelationshipEntity("FUNCTION_CONTAINS_VARIABLE")
public class FunctionContainsVariable implements Relation {

	private static final long serialVersionUID = -9177342333868580563L;

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public Long getId() {
		return id;
	}

	public FunctionContainsVariable() {
		super();
	}

	public FunctionContainsVariable(Function function, Variable containVariable) {
		super();
		this.function = function;
		this.containVariable = containVariable;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	private Function function;
	private Variable containVariable;

	@Override
	public Long getStartNodeGraphId() {
		return function.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return containVariable.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_CONTAINS_VARIABLE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
