package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
@NodeEntity
@EqualsAndHashCode
@NoArgsConstructor
public class Type implements Node {
    @Id
    @GeneratedValue
    private Long id;

	private String typeName;
	
	private String aliasName;
	
    private Long entityId;
	private static final long serialVersionUID = 6805501035295416590L;
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("typeName", getTypeName() == null ? "" : getTypeName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("aliasName", getAliasName() == null ? (getTypeName() == null ? "" : getTypeName()) : getAliasName());
		return properties;
	}
	
	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Type;
	}

	
	public static final String LABEL_INDEX = "typeName";
	@Override
	public String indexName() {
		return LABEL_INDEX;
	}
}
