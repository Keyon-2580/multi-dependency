package cn.edu.fudan.se.multidependency.model.relation;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_DEPEND_ON)
@EqualsAndHashCode
public class DependOn implements Relation {
	
	private static final long serialVersionUID = 6381791099417646137L;

	@Id
    @GeneratedValue
    private Long id;
	
    @StartNode
	private Node startNode;

    @EndNode
	private Node endNode;
	
	private int times;
	
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
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}

}
