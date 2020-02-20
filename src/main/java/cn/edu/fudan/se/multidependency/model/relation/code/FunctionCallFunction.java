package cn.edu.fudan.se.multidependency.model.relation.code;

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

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FUNCTION_CALL_FUNCTION)
public class FunctionCallFunction implements Relation {
	
	private static final long serialVersionUID = 5982413005555063698L;

	@StartNode
	private Function function;
	
	@EndNode
	private Function callFunction;
	
	public FunctionCallFunction(Function function, Function callFunction) {
		super();
		this.function = function;
		this.callFunction = callFunction;
	}

	@Id
    @GeneratedValue
    private Long id;
	
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
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}
}
