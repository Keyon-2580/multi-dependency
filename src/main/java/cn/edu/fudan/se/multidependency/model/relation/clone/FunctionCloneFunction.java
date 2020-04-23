package cn.edu.fudan.se.multidependency.model.relation.clone;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 边视作没有方向
 * @author fan
 *
 */
@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FUNCTION_CLONE_FUNCTION)
public class FunctionCloneFunction implements Relation {
	
	private static final long serialVersionUID = -5264263917272265233L;

	@Id
    @GeneratedValue
    private Long id;
	 	
	@StartNode
	private Function function1;
	
	@EndNode
	private Function function2;
	
	public FunctionCloneFunction(Function function1, Function function2) {
		this.function1 = function1;
		this.function2 = function2;
	}
	
	@Override
	public Long getStartNodeGraphId() {
		return function1.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return function2.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_CLONE_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}
	
	
}
