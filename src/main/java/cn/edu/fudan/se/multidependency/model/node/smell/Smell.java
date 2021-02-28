package cn.edu.fudan.se.multidependency.model.node.smell;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NodeEntity
@NoArgsConstructor
public class Smell implements Node {

	@Id
    @GeneratedValue
    private Long id;

	private String name;

	private Long entityId;

	private int size;

	private String language;

	private NodeLabelType level;

	private SmellType type;

	public Smell(String name) {
		this.name = name;
		this.size = 0;
		this.entityId = -1L;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("size", getSize());
		properties.put("language", getLanguage() == null ? "" : getLanguage());
		properties.put("level", getLevel() == null ? "" : getLevel());
		properties.put("type", getType() == null ? "" : getType());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Smell;
	}

	@Transient
	private Set<Node> nodes = new HashSet<>();

	@Transient
	private Set<Relation> relations = new HashSet<>();
	
	public synchronized void addNode(Node node) {
		this.nodes.add(node);
	}
	
	public synchronized void addRelation(Relation relation) {
		this.relations.add(relation);
	}
	
	public int sizeOfNodes() {
		return this.nodes.size();
	}

}
