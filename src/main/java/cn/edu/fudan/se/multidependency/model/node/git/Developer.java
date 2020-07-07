package cn.edu.fudan.se.multidependency.model.node.git;

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
public class Developer implements Node {

	private static final long serialVersionUID = -4656210021076869445L;

	@Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private String name;

	public Developer(Long entityId, String name){
		this.entityId = entityId;
		this.name = name;
	}

    @Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Developer;
	}

}
