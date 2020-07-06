package cn.edu.fudan.se.multidependency.model.node.clone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class CloneGroup implements Node {
	
	private static final long serialVersionUID = -8494229666439859350L;

	@Id
    @GeneratedValue
    private Long id;
	
	private String name;

	private Long entityId;
	
	private int size;
	
	private String language;
	
	private String cloneLevel;
	
	public CloneGroup(String name) {
		this.name = name;
		this.size = -1;
		this.entityId = -1L;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("size", getSize());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("cloneLevel", getCloneLevel() == null ? "" : getCloneLevel());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.CloneGroup;
	}

	@Override
	public String indexName() {
		return null;
	}
	
	@Transient
	private Set<CodeNode> nodes = new HashSet<>();

	@Transient
	private Set<Clone> relations = new HashSet<>();
	
	public void addNode(CodeNode node) {
		this.nodes.add(node);
	}
	
	public void addRelation(Clone relation) {
		this.relations.add(relation);
	}
	
	public int sizeOfNodes() {
		return this.nodes.size();
	}

}
