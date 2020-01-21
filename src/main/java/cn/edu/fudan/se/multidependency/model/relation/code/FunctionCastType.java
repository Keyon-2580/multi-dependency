package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity(RelationType.str_FUNCTION_CAST_TYPE)
@Data
@NoArgsConstructor
public class FunctionCastType implements Relation {

	private static final long serialVersionUID = -7384669294027502528L;

	@Id
    @GeneratedValue
    private Long id;
	
	private Function function;
	
	private Type castType;

	public FunctionCastType(Function function, Type castType) {
		super();
		this.function = function;
		this.castType = castType;
	}

	@Override
	public Long getStartNodeGraphId() {
		return function.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return castType.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_CAST_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
