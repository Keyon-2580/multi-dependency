package fan.md.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import fan.md.model.node.Node;
import fan.md.model.node.NodeType;

@NodeEntity
public class Type implements Node {
    @Id
    @GeneratedValue
    private Long id;

	private String typeName;
	
	private String packageName;

	private Long parentId;
	
    private int entityId;
	private static final long serialVersionUID = 6805501035295416590L;
	
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getEntityId() {
		return entityId;
	}
	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("typeName", getTypeName());
		properties.put("entityId", getEntityId());
		properties.put("packageName", getPackageName());
		return properties;
	}
	
	@Override
	public NodeType getNodeType() {
		return NodeType.Type;
	}

	@Override
	public Long getParentId() {
		return parentId == null ? -1L : parentId;
	}
	
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

}
