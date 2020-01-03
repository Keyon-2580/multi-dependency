package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.testcase.Scenario;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_NODE_IS_SCENARIO)
public class NodeIsScenario implements Relation {

	private static final long serialVersionUID = 2374756803673911821L;

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	private Node startNode;
	
	private Scenario scenario;

	@Override
	public Long getStartNodeGraphId() {
		return startNode.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return scenario.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.NODE_IS_SCENARIO;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	public Node getStartNode() {
		return startNode;
	}

	public void setStartNode(Node startNode) {
		this.startNode = startNode;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

}
