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
@RelationshipEntity(RelationType.str_RETURN)
public class Return implements RelationWithTimes {

	private static final long serialVersionUID = -3315100529955945595L;

	@StartNode
	private Function function;
	
	@EndNode
	private Type returnType;
	
	private int times = 1;
	
	public Return(Function function, Type returnType) {
		super();
		this.function = function;
		this.returnType = returnType;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public Node getStartNode() {
		return function;
	}

	@Override
	public Node getEndNode() {
		return returnType;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.RETURN;
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
