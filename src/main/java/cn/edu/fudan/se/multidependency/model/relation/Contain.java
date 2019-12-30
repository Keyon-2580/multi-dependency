package cn.edu.fudan.se.multidependency.model.relation;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;

@RelationshipEntity(RelationType.str_CONTAIN)
public class Contain implements Relation {

	private static final long serialVersionUID = 6713953591550916427L;

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
	
	private Node start;
	
	private Node end;

	@Override
	public Long getStartNodeGraphId() {
		return start.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return end.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.CONTAIN;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}
	
}
