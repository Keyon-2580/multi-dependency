package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity(RelationType.str_VARIABLE_TYPE_PARAMETER_TYPE)
@Data
@NoArgsConstructor
public class VariableTypeParameterType implements Relation {

	private static final long serialVersionUID = 2157443508230175654L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Variable variable;
	
	@EndNode
	private Type type;
	
	@Override
	public Long getStartNodeGraphId() {
		return variable.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return type.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.VARIABLE_TYPE_PARAMETER_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
