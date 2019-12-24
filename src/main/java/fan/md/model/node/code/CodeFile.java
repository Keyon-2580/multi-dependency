package fan.md.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import fan.md.model.node.Node;
import fan.md.model.node.NodeType;

@NodeEntity("File")
public class CodeFile implements Node {
	
	private static final long serialVersionUID = -8736926263545574636L;

    @Id
    @GeneratedValue
    private Long id;
    
    private int entityId;
    
	private String fileName;
	
	private String path;
	
	private Long parentId;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
		properties.put("fileName", getFileName());
		properties.put("entityId", getEntityId());
		properties.put("path", getPath());
		properties.put("parentId", getParentId());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.File;
	}

	@Override
	public Long getParentId() {
		return parentId == null ? -1L : parentId;
	}
	
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

}
