package cn.edu.fudan.se.multidependency.model.relation.clone;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FUNCTION_CLONE_FUNCTION)
public class FunctionCloneFunction implements CloneRelation {
	
	private static final long serialVersionUID = -5264263917272265233L;

	@Id
    @GeneratedValue
    private Long id;
	 	
	@StartNode
	private Function function1;
	
	@EndNode
	private Function function2;
	
	private double value;
	
	private int node1Index;
	
	private int node2Index;
	
	private int node1StartLine;
	
	private int node1EndLine;
	
	private int node2StartLine;
	
	private int node2EndLine;
	
	private String cloneType;
	
	public FunctionCloneFunction(Function function1, Function function2) {
		this.function1 = function1;
		this.function2 = function2;
	}
	
	@Override
	public Node getStartNode() {
		return function1;
	}

	@Override
	public Node getEndNode() {
		return function2;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FUNCTION_CLONE_FUNCTION;
	}

}
