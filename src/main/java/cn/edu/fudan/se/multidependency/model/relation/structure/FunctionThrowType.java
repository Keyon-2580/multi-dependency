package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FUNCTION_THORW_TYPE)
public class FunctionThrowType implements RelationWithTimes {

	private static final long serialVersionUID = -5858763297137981586L;
	
	@Id
    @GeneratedValue
    private Long id;

	public FunctionThrowType(Function function, Type type) {
		super();
		this.function = function;
		this.type = type;
	}

	@StartNode
	private Function function;
	
	@EndNode
	private Type type;
	
	private int times = 1;

	@Override
	public Node getStartNode() {
		return function;
	}

	@Override
	public Node getEndNode() {
		return type;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_THROW_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}
	
	public void addTimes() {
		this.times++;
	}

}
