package fan.md.model.relation.code;

import java.io.Serializable;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import fan.md.model.entity.code.Function;
import fan.md.model.entity.code.Type;

@RelationshipEntity("TYPE_CONTAINS_FUNCTION")
public class TypeContainsFunction implements Serializable {
	
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
	
}
