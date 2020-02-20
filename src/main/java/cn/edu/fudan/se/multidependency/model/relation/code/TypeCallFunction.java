package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_TYPE_CALL_FUNCTION)
public class TypeCallFunction implements Relation {
	
	private static final long serialVersionUID = 5982413005555063698L;

	@StartNode
	private Type type;
	
	@EndNode
	private Function callFunction;
	
	public TypeCallFunction(Type type, Function callFunction) {
		super();
		this.type = type;
		this.callFunction = callFunction;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public Long getStartNodeGraphId() {
		return type.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return callFunction.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TYPE_CALL_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}
}
