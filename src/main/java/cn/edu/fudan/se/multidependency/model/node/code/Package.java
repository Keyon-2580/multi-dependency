package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.NodeType;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;

/**
 * java：包
 * c/c++：目录
 * @author fan
 *
 */
@NodeEntity
public class Package implements Node {

    @Id
    @GeneratedValue
    private Long id;
    
	private String packageName;
	
	private boolean isDirectory;

    private Long entityId;

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

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("packageName", getPackageName() == null ? "" : getPackageName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
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

}
