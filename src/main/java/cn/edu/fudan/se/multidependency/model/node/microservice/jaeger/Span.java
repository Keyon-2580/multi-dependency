package cn.edu.fudan.se.multidependency.model.node.microservice.jaeger;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import lombok.Data;

@Data
public class Span implements Node {

	private static final long serialVersionUID = 1083944968038564253L;
	
	@Id
    @GeneratedValue
    private Long id;

    private Long entityId;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> map = new HashMap<>();
		
		return map;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Span;
	}

}
