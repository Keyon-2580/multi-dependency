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
public class Snippet implements CodeNode {
	
	private static final long serialVersionUID = -2425172282148281962L;

	@Id
    @GeneratedValue
    private Long id;

	private String name;
	
	private Long entityId;
    
	private int startLine = -1;
	
	private int endLine = -1;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("startLine", getStartLine());
		properties.put("endLine", getEndLine());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Snippet;
	}

	@Override
	public String indexName() {
		return null;
	}

	@Override
	public String getIdentifier() {
		return getName();
	}

	@Override
	public String getIdentifierSimpleName() {
		return getName();
	}

	@Override
	public String getIdentifierSuffix() {
		return Constant.CODE_NODE_IDENTIFIER_SUFFIX_SNIPPET;
	}

	@Override
	public void setIdentifier(String identifier) {
		// TODO Auto-generated method stub
		
	}
}
