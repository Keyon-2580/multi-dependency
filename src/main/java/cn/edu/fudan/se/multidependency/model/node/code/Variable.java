package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;

@Data
@NodeEntity
@EqualsAndHashCode
@NoArgsConstructor
public class Variable implements Node {
	
	private static final long serialVersionUID = 7656480620809763012L;

	private String name;
	
	private String simpleName;

	private Long entityId;
	
	private String typeIdentify;
	
	/**
	 * 是否为类的属性
	 */
	private boolean field;
	
    @Id
    @GeneratedValue
    private Long id;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("simpleName", getSimpleName() == null ? "" : getSimpleName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("typeIdentify", getTypeIdentify() == null ? "" : getTypeIdentify());
		properties.put("field", isField());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Variable;
	}
	
	public static final String LABEL_INDEX = "variableName";
	@Override
	public String indexName() {
		return LABEL_INDEX;
	}
}
