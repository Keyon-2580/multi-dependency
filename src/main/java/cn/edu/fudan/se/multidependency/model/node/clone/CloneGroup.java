package cn.edu.fudan.se.multidependency.model.node.clone;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class CloneGroup implements Node {
	
	public static final CloneGroup ALL_CLONE_GROUP_FILE = new CloneGroup("group_all", CloneLevel.file);
	
	public static final CloneGroup ALL_CLONE_GROUP_FUNCTION = new CloneGroup("group_all", CloneLevel.function);
	
	public static CloneGroup allGroup(CloneLevel level) {
		if(level == CloneLevel.function) {
			return ALL_CLONE_GROUP_FUNCTION;
		} else {
			return ALL_CLONE_GROUP_FILE;
		}
	}

	private static final long serialVersionUID = -8494229666439859350L;

	@Id
    @GeneratedValue
    private Long id;
	
	private String name;

	private Long entityId;
	
	private String level;
	
	private int size;
	
	public CloneGroup(String name, CloneLevel level) {
		this.id = Long.MIN_VALUE;
		this.level = level == CloneLevel.function ? NodeLabelType.Function.toString() : NodeLabelType.ProjectFile.toString();
		this.name = name;
		this.size = -1;
		this.entityId = -1L;
	}
	
	public void setLevel(NodeLabelType label) {
		this.level = label.toString();
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("level", getLevel() == null ? "" : getLevel());
		properties.put("size", getSize());
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

}
