package fan.md.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import fan.md.model.node.Node;
import fan.md.model.node.NodeType;

@NodeEntity("Variable")
public class Variable implements Node {

	private static final long serialVersionUID = 7656480620809763012L;

	private Long parentId;
	
	private String variableName;

	private int entityId;
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
		properties.put("variableName", getVariableName());
		properties.put("parentId", getParentId());
		properties.put("entityId", entityId);
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Variable;
	}

	@Override
	public Long getParentId() {
		return parentId == null ? -1 : parentId;
	}
	
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}
}
