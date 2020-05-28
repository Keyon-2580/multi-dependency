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
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FUNCTION_IMPLEMENT_FUNCTION)
@EqualsAndHashCode
public class FunctionImplementFunction implements Relation {
	
	private static final long serialVersionUID = 7582417525375943056L;

	@Id
    @GeneratedValue
    private Long id;
	
	public FunctionImplementFunction(Function function, Function implementFunction) {
		this.function = function;
		this.implementFunction = implementFunction;
	}
	
	@StartNode
	private Function function;
	
	@EndNode
	private Function implementFunction;

	@Override
	public Node getStartNode() {
		return function;
	}

	@Override
	public Node getEndNode() {
		return implementFunction;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_IMPLEMENT_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}
	
}
