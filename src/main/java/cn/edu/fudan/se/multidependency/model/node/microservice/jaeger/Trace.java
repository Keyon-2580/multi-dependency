package cn.edu.fudan.se.multidependency.model.node.microservice.jaeger;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import lombok.Data;

@Data
@NodeEntity
public class Trace implements Node {
	
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
    private Long id;

    private Long entityId;
    
    private String traceId;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("traceId", getTraceId() == null ? "" : getTraceId());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Trace;
	}

}
