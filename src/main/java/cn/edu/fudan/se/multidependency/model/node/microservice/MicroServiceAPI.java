package cn.edu.fudan.se.multidependency.model.node.microservice;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class MicroServiceAPI implements Node {
	
	private static final long serialVersionUID = -404310425549237045L;

	@Id
    @GeneratedValue
    private Long id;

	private Long entityId;
	
	private String apiName;

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entity", getEntityId() == null ? -1 : getEntityId());
		properties.put("apiName", getApiName() == null ? "" : getApiName());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.MicroServiceAPI;
	}

	public static final String LABEL_INDEX = "apiName";
	@Override
	public String indexName() {
		return LABEL_INDEX;
	}

}
