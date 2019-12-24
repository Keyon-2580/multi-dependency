package fan.md.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import fan.md.model.node.Node;
import fan.md.model.node.NodeType;

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
