package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.NoArgsConstructor;

@NodeEntity
@Data
@NoArgsConstructor
public class Namespace implements CodeUnit {
	
	private static final long serialVersionUID = 7914006834768560932L;

    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    
    private Long entityId;
    
    private String identifier;
    
    private String simpleName;
	
	private int startLine = -1;
	
	private int endLine = -1;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("identifier", getIdentifier() == null ? "" : getIdentifier());
		properties.put("simpleName", getSimpleName() == null ? "" : getSimpleName());
		properties.put("startLine", getStartLine());
		properties.put("endLine", getEndLine());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Namespace;
	}
	
	public static final String LABEL_INDEX = "namespaceName";
	@Override
	public String indexName() {
		return LABEL_INDEX;
	}

	@Override
	public String getIdentifierSuffix() {
		return Constant.CODE_NODE_IDENTIFIER_SUFFIX_NAMESPACE;
	}

	@Override
	public String getIdentifierSimpleName() {
		return getSimpleName();
	}

}
