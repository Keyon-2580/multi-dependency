package fan.md.model.relation.dynamic;

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

@RelationshipEntity("DYNAMIC_FUNCTION_CALL_FUNCTION")
public class FunctionDynamicCallFunction implements Relation {

	private static final long serialVersionUID = -7640490954063715746L;
	
	@StartNode
	private Function function;
	
	@EndNode
	private Function callFunction;
	
	@Id
    @GeneratedValue
    private Long id;
	
	private Map<String, Object> properties = new HashMap<>();
	
	public void setCallOrder(String order) {
		properties.put("order", order);
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
		return callFunction.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.DYNAMIC_FUNCTION_CALL_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}
	
}
