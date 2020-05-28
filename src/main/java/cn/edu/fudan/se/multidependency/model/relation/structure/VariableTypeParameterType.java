package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity(RelationType.str_VARIABLE_TYPE_PARAMETER_TYPE)
@Data
@NoArgsConstructor
public class VariableTypeParameterType implements RelationWithTimes {

	private static final long serialVersionUID = 2157443508230175654L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Variable variable;
	
	@EndNode
	private Type type;
	
	private int times = 1;
	
	@Override
	public Node getStartNode() {
		return variable;
	}

	@Override
	public Node getEndNode() {
		return type;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.VARIABLE_TYPE_PARAMETER_TYPE;
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
