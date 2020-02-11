package cn.edu.fudan.se.multidependency.model.node.testcase;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import lombok.Data;
import lombok.NoArgsConstructor;

@NodeEntity
@Data
@NoArgsConstructor
public class Feature implements Node {

	private static final long serialVersionUID = -2410710967921462154L;

	private String featureName;
	
    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private Integer featureId;
    
    private String description;
    
    private String traceId;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("featureName", getFeatureName() == null ? "" : getFeatureName());
		properties.put("featureId", getFeatureId() == null ? -1 : getFeatureId());
		properties.put("description", getDescription() == null ? "" : getDescription());
		properties.put("traceId", getTraceId() == null ? "" : getTraceId());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Feature;
	}

}
