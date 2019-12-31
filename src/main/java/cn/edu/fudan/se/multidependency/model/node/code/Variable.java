package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.NodeType;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;

@NodeEntity("Variable")
public class Variable implements Node {

	private static final long serialVersionUID = 7656480620809763012L;

	private String variableName;

	private Long entityId;
	
	private String typeIdentify;
	
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

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("variableName", getVariableName() == null ? "" : getVariableName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("typeIdentify", getTypeIdentify() == null ? "" : getTypeIdentify());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Variable;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public String getTypeIdentify() {
		return typeIdentify;
	}

	public void setTypeIdentify(String typeIdentify) {
		this.typeIdentify = typeIdentify;
	}
}
