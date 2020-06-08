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

	private static final long serialVersionUID = -8494229666439859350L;

	@Id
    @GeneratedValue
    private Long id;
	
	private String name;

	private Long entityId;
	
	private String group;
	
	private String level;
	
	public void setLevel(NodeLabelType label) {
		this.level = label.toString();
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("group", getGroup() == null ? "" : getGroup());
		properties.put("level", getLevel() == null ? "" : getLevel());
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
