package cn.edu.fudan.se.multidependency.model.relation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.Transient;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_DEPEND_ON)
public class DependOn implements Relation {
	
	private static final long serialVersionUID = 6381791099417646137L;

    private Long id;
	
	private Node startNode;
	
	private Node endNode;
	
	@Transient
	private Map<RelationType, List<Relation>> relations = new HashMap<>();

	public DependOn(Node startNode, Node endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}
	
	@Override
	public Node getStartNode() {
		return startNode;
	}

	@Override
	public Node getEndNode() {
		return endNode;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.DEPEND_ON;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
