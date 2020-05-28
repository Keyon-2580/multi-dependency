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
@RelationshipEntity(RelationType.str_TYPE_CALL_FUNCTION)
public class TypeCallFunction implements RelationWithTimes {
	
	private static final long serialVersionUID = 5982413005555063698L;

	@StartNode
	private Type type;
	
	@EndNode
	private Function callFunction;
	
	private int times = 1;
	
	public TypeCallFunction(Type type, Function callFunction) {
		super();
		this.type = type;
		this.callFunction = callFunction;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public Node getStartNode() {
		return type;
	}

	@Override
	public Node getEndNode() {
		return callFunction;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TYPE_CALL_FUNCTION;
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
