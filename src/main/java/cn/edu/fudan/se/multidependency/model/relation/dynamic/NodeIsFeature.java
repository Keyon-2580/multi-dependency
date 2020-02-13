package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity(RelationType.str_NODE_IS_FEATURE)
@Data
@NoArgsConstructor
public class NodeIsFeature implements Relation {

	private static final long serialVersionUID = -6943775631006502053L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Node startNode;
	
	@EndNode
	private Feature feature;

	@Override
	public Long getStartNodeGraphId() {
		return startNode.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return feature.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.NODE_IS_FEATURE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
