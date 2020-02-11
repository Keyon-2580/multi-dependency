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
public class Span implements Node {

	private static final long serialVersionUID = 1083944968038564253L;
	
	@Id
    @GeneratedValue
    private Long id;

    private Long entityId;
    
    private String traceId;
    
    private String spanId;
    
    private String serviceName;
    
    private String operationName;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("spanId", getSpanId() == null ? "" : getSpanId());
		properties.put("traceId", getTraceId() == null ? "" : getTraceId());
		properties.put("serviceName", getServiceName() == null ? "" : getServiceName());
		properties.put("operationName", getOperationName() == null ? "" : getOperationName());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Span;
	}

}
