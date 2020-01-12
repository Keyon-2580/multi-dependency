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
public class Scenario implements Node {

	private static final long serialVersionUID = 2260001955112320935L;

	private String scenarioName;
	
    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("scenarioName", getScenarioName() == null ? "" : getScenarioName());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Scenario;
	}

}
