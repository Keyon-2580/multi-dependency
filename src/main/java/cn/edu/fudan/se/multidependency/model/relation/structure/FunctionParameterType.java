package cn.edu.fudan.se.multidependency.model.relation.structure;

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
@RelationshipEntity(RelationType.str_FUNCTION_PARAMETER_TYPE)
public class FunctionParameterType implements Relation {

	private static final long serialVersionUID = -8796616144049338126L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Function function;
	
	@EndNode
	private Type parameterType;

	public FunctionParameterType(Function function, Type parameterType) {
		super();
		this.function = function;
		this.parameterType = parameterType;
	}

	@Override
	public Long getStartNodeGraphId() {
		return function.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return parameterType.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_PARAMETER_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}