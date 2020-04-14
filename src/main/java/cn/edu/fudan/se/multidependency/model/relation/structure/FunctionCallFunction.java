package cn.edu.fudan.se.multidependency.model.relation.structure;

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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FUNCTION_CALL_FUNCTION)
@EqualsAndHashCode
public class FunctionCallFunction implements Relation {
	
	private static final long serialVersionUID = 5982413005555063698L;

	@StartNode
	private Function function;
	
	@EndNode
	private Function callFunction;
	
	private Integer times;
	
	public FunctionCallFunction(Function function, Function callFunction) {
		super();
		this.function = function;
		this.callFunction = callFunction;
		this.times = 1;
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
	
	public void addTimes() {
		this.times++;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_CALL_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes() == null ? 1 : getTimes());
		return properties;
	}
}