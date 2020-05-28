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
@RelationshipEntity(RelationType.str_FUNCTION_IMPLLINK_FUNCTION)
@EqualsAndHashCode
public class FunctionImplLinkFunction implements Relation {
	private static final long serialVersionUID = -5543957038003318L;

	@Id
    @GeneratedValue
    private Long id;
	
	public FunctionImplLinkFunction(Function function, Function impllinkFunction) {
		this.function = function;
		this.impllinkFunction = impllinkFunction;
	}
	
	@StartNode
	private Function function;
	
	@EndNode
	private Function impllinkFunction;

	@Override
	public Node getStartNode() {
		return function;
	}

	@Override
	public Node getEndNode() {
		return impllinkFunction;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_IMPLLINK_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}

}
