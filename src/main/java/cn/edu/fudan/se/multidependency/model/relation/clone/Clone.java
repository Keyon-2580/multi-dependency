package cn.edu.fudan.se.multidependency.model.relation.clone;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.CodeUnit;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_CLONE)
public class Clone implements CloneRelation {
	private static final long serialVersionUID = 8708817258770543568L;

	@Id
    @GeneratedValue
    private Long id;
	 	
	@StartNode
	private CodeUnit codeUnit1;
	
	@EndNode
	private CodeUnit codeUnit2;
	
	private double value;
	
	private int node1Index;
	
	private int node2Index;
	
	private int node1StartLine;
	
	private int node1EndLine;
	
	private int node2StartLine;
	
	private int node2EndLine;
	
	private String cloneType;

	@Override
	public Node getStartNode() {
		return codeUnit1;
	}

	@Override
	public Node getEndNode() {
		return codeUnit2;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.CLONE;
	}
}
