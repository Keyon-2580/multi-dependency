package cn.edu.fudan.se.multidependency.model.relation.clone;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_TYPE_CLONE_TYPE)
public class TypeCloneType implements CloneRelation {
	
	private static final long serialVersionUID = 3696125356842103464L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Type type1;
	
	@EndNode
	private Type type2;
	
	private double value;
	
	private int node1Index;
	
	private int node2Index;
	
	private int node1StartLine;
	
	private int node1EndLine;
	
	private int node2StartLine;
	
	private int node2EndLine;
	
	public TypeCloneType(Type type1, Type type2) {
		this.type1 = type1;
		this.type2 = type2;
	}

	@Override
	public Node getStartNode() {
		return type1;
	}

	@Override
	public Node getEndNode() {
		return type2;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TYPE_CLONE_TYPE;
	}

}
