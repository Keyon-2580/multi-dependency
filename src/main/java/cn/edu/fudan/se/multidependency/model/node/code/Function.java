package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.NodeType;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;

@NodeEntity
public class Function implements Node {

	private static final long serialVersionUID = 6993550414163132668L;
	
	@Id
    @GeneratedValue
    private Long id;
	
    private Integer entityId;

	private String functionName;
	
	public String getFunctionName() {
		return functionName;
	}
	
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("functionName", getFunctionName() == null ? "" : getFunctionName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Function;
	}

}
