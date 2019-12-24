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

@RelationshipEntity("FUNCTION_CONTAINS_TYPE")
public class FunctionContainsType implements Relation {

	private static final long serialVersionUID = 4645473697153791270L;

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
	
	private Function function;
	private Type containType;

	@Override
	public Long getStartNodeGraphId() {
		return function.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return containType.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_CONTAINS_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
