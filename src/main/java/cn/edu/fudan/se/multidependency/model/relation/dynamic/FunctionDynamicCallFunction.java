package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity(RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION)
@Data
@NoArgsConstructor
public class FunctionDynamicCallFunction implements Relation {

	private static final long serialVersionUID = -7640490954063715746L;
	
	@StartNode
	private Function function;
	
	@EndNode
	private Function callFunction;
	
	public FunctionDynamicCallFunction(Function function, Function callFunction) {
		super();
		this.function = function;
		this.callFunction = callFunction;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	private String order;
	
	private String traceId;
	
	private String spanId;
	
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
		Map<String, Object> properties = new HashMap<>();
		properties.put("order", getOrder() == null ? "" : getOrder());
		return properties;
	}

}
