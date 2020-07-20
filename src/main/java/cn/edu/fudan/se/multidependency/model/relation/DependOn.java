package cn.edu.fudan.se.multidependency.model.relation;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_DEPEND_ON)
public class DependOn implements Relation {
	
	private static final long serialVersionUID = 6381791099417646137L;

    private Long id;
	
//	private int times;
	
	private Node startNode;
	
	private Node endNode;

	public DependOn(Node startNode, Node endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
	}
	
	/*@Override
	public void addTimes() {
		times++;
	}*/

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
