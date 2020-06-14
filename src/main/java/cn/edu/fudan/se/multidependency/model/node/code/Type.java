package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@EqualsAndHashCode
@NoArgsConstructor
public class Type implements CodeNode {
	
	private static final long serialVersionUID = 6805501035295416590L;
	
    @Id
    @GeneratedValue
    private Long id;

	private String name;
	
	private String aliasName;
	
    private Long entityId;
    
    private String identifier;
    
    private String simpleName;
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("aliasName", getAliasName() == null ? (getName() == null ? "" : getName()) : getAliasName());
		properties.put("identifier", getIdentifier() == null ? "" : getIdentifier());
		properties.put("simpleName", getSimpleName() == null ? "" : getSimpleName());
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

	@Override
	public String getIdentifierSuffix() {
		return Constant.CODE_NODE_IDENTIFIER_SUFFIX_TYPE;
	}

	@Override
	public String getIdentifierSimpleName() {
		return getSimpleName();
	}
}
