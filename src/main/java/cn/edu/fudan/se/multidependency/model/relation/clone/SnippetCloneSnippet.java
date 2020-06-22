package cn.edu.fudan.se.multidependency.model.relation.clone;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_SNIPPET_CLONE_SNIPPET)
public class SnippetCloneSnippet implements CloneRelation {
	
	private static final long serialVersionUID = 4425177005028205413L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Snippet snippet1;
	
	@EndNode
	private Snippet snippet2;
	
	public SnippetCloneSnippet(Snippet snippet1, Snippet snippet2) {
		this.snippet1 = snippet1;
		this.snippet2 = snippet2;
	}
	
	private double value;
	
	private int node1Index;
	
	private int node2Index;
	
	private int node1StartLine;
	
	private int node1EndLine;
	
	private int node2StartLine;
	
	private int node2EndLine;

	@Override
	public Node getStartNode() {
		return snippet1;
	}

	@Override
	public Node getEndNode() {
		return snippet2;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.SNIPPET_CLONE_SNIPPET;
	}
}
