package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity()
@Data
@NoArgsConstructor
public class ServiceCallService implements Relation {

	private static final long serialVersionUID = -4247722482556120383L;
	
	@Id
    @GeneratedValue
    private Long id;
	
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
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
