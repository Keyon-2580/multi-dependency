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
public class MicroService implements Node {

	private static final long serialVersionUID = 8307013466383536758L;
	
	@Id
    @GeneratedValue
    private Long id;

    private Long entityId;
    
    private String serviceGroupName;
    
    /**
     * 名字在数据库中唯一
     */
    private String name;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("name", getName() == null ? "" : getName());
		properties.put("serviceGroupName", getServiceGroupName() == null ? "" : getServiceGroupName());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.MicroService;
	}
	
	public static final String LABEL_INDEX = "name";

}
