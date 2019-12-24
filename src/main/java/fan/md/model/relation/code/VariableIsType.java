package fan.md.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import fan.md.model.node.code.Type;
import fan.md.model.node.code.Variable;
import fan.md.model.relation.Relation;
import fan.md.model.relation.RelationType;

@RelationshipEntity("VARIABLE_IS_TYPE")
public class VariableIsType implements Relation {
	private static final long serialVersionUID = 1767344862220786333L;
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
	
	public VariableIsType() {
		super();
	}

	public VariableIsType(Variable variable, Type type) {
		super();
		this.variable = variable;
		this.type = type;
	}

	private Variable variable;
	private Type type;

	@Override
	public Long getStartNodeGraphId() {
		return variable.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return type.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.VARIABLE_IS_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
}
