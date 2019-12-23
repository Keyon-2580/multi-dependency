package fan.md.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import fan.md.model.node.code.Function;
import fan.md.model.node.code.Type;
import fan.md.model.relation.Relation;
import fan.md.model.relation.RelationType;

@RelationshipEntity("TYPE_CONTAINS_FUNCTION")
public class TypeContainsFunction implements Relation {
	
	private static final long serialVersionUID = -7683024111434170111L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Type type;
	
	@EndNode
	private Function function;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	@Override
	public Long getStartNodeGraphId() {
		return type.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return function.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TYPE_CONTAINS_FUNCTION;
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
