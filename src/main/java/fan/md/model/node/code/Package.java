package fan.md.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import fan.md.model.node.Node;
import fan.md.model.node.NodeType;

@NodeEntity
public class Package implements Node {

    @Id
    @GeneratedValue
    private Long id;
	private String packageName;
	
	private boolean isDirectory;

    private int entityId;

	private Long parentId;
	
	private static final long serialVersionUID = -4892461872164624064L;

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
		properties.put("packageName", getPackageName());
		properties.put("entityId", getEntityId());
		properties.put("isDirectory", isDirectory());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Package;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	@Override
	public Long getParentId() {
		return parentId == null ? -1L : parentId;
	}
	
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

}
