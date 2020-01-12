package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
@Data
@NoArgsConstructor
public class Type implements Node {
    @Id
    @GeneratedValue
    private Long id;

	private String typeName;
	
	private String packageName;
	
	private String aliasName;
	
	private String inFilePath;

    private Long entityId;
	private static final long serialVersionUID = 6805501035295416590L;
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("typeName", getTypeName() == null ? "" : getTypeName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("packageName", getPackageName() == null ? "" : getPackageName());
		properties.put("aliasName", getAliasName() == null ? (getTypeName() == null ? "" : getTypeName()) : getAliasName());
		properties.put("inFilePath", getInFilePath() == null ? "" : getInFilePath());
		return properties;
	}
	
	@Override
	public NodeType getNodeType() {
		return NodeType.Type;
	}

}
