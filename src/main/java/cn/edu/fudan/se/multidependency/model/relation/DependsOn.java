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
@RelationshipEntity(RelationType.str_DEPENDS_ON)
@EqualsAndHashCode
public class DependsOn implements Relation, RelationWithTimes{
	
	private static final long serialVersionUID = 6381791099417646137L;

	@Id
    @GeneratedValue
    private Long id;
	
    @StartNode
	private Node startNode;

    @EndNode
	private Node endNode;
	
	private int times;

	private String dependsOnType;
	
	public DependsOn(Node startNode, Node endNode) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.dependsOnType = "";
	}
	public DependsOn(Node startNode, Node endNode, String dependsOnType) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.dependsOnType = dependsOnType;
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
		return RelationType.DEPENDS_ON;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		properties.put("dependsOnType", getDependsOnType());
		return properties;
	}

	@Override
	public void addTimes() {
		this.times++;
	}

	public void adDependsOnType(String dependsOnType) {
		this.dependsOnType +="__"+ dependsOnType;
	}
}
