package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.NoArgsConstructor;

@NodeEntity
@Data
@NoArgsConstructor
public class Namespace implements Node {
	
	private static final long serialVersionUID = 7914006834768560932L;

    @Id
    @GeneratedValue
    private Long id;
    
    private String namespaceName;
    
    private Long entityId;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("namespaceName", getNamespaceName() == null ? "" : getNamespaceName());
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

}
