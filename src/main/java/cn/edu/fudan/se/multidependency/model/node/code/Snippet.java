package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneRelationNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@EqualsAndHashCode
@NoArgsConstructor
public class Snippet implements Node, CloneRelationNode, NodeWithLine {
	
	private static final long serialVersionUID = -2425172282148281962L;

	@Id
    @GeneratedValue
    private Long id;

	private String name;
	
	private Long entityId;
    
	private int startLine = -1;
	
	private int endLine = -1;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("startLine", getStartLine());
		properties.put("endLine", getEndLine());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Snippet;
	}

	@Override
	public String indexName() {
		return null;
	}

	@Override
	public CloneLevel getCloneLevel() {
		return CloneLevel.snippet;
	}
}
