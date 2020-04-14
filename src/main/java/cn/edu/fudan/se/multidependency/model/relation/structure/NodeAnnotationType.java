package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_NODE_ANNOTATION_TYPE)
public class NodeAnnotationType implements Relation {

	private static final long serialVersionUID = 8248026322068428052L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	public NodeAnnotationType(Node startNode, Type annotationType) {
		super();
		this.startNode = startNode;
		this.annotationType = annotationType;
	}

	@StartNode
	private Node startNode;
	
	@EndNode
	private Type annotationType;

	@Override
	public Long getStartNodeGraphId() {
		return startNode.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return annotationType.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.NODE_ANNOTATION_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}