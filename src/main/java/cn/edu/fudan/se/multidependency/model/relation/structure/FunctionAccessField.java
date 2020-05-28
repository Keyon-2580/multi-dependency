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
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FUNCTION_ACCESS_FIELD)
@EqualsAndHashCode
public class FunctionAccessField implements RelationWithTimes {
	
	private static final long serialVersionUID = -2911695752320415027L;

	@StartNode
	private Function function;
	
	@EndNode
	private Variable field;
	
	private int times = 1;

	@Id
    @GeneratedValue
    private Long id;
	
	public FunctionAccessField(Function function, Variable field) {
		this.function = function;;
		this.field = field;
		this.times = 1;
	}
	
	public void addTimes() {
		this.times++;
	}

	@Override
	public Node getStartNode() {
		return function;
	}

	@Override
	public Node getEndNode() {
		return field;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_ACCESS_FIELD;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}
	
}
