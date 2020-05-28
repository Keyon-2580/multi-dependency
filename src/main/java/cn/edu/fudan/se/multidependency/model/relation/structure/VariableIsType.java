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
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity(RelationType.str_VARIABLE_IS_TYPE)
@Data
@NoArgsConstructor
public class VariableIsType implements Relation {
	private static final long serialVersionUID = 1767344862220786333L;
	@Id
    @GeneratedValue
    private Long id;

	public VariableIsType(Variable variable, Type type) {
		super();
		this.variable = variable;
		this.type = type;
	}

	@StartNode
	private Variable variable;
	
	@EndNode
	private Type type;

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
		return RelationType.VARIABLE_IS_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
