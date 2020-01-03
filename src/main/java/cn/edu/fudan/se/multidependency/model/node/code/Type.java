package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Type implements Node {
    @Id
    @GeneratedValue
    private Long id;

	private String typeName;
	
	private String packageName;
	
	private String aliasName;

    private Long entityId;
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
	public Long getEntityId() {
		return entityId;
	}
	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("typeName", getTypeName() == null ? "" : getTypeName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("packageName", getPackageName() == null ? "" : getPackageName());
		properties.put("aliasName", getAliasName() == null ? (getTypeName() == null ? "" : getTypeName()) : getAliasName());
		return properties;
	}
	
	@Override
	public NodeType getNodeType() {
		return NodeType.Type;
	}
	public String getAliasName() {
		return aliasName;
	}
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
	@Override
	public String toString() {
		return "Type [id=" + id + ", typeName=" + typeName + ", packageName=" + packageName + ", aliasName=" + aliasName
				+ ", entityId=" + entityId + "]";
	}

}
