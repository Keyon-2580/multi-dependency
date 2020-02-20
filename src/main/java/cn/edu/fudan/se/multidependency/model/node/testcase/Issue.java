package cn.edu.fudan.se.multidependency.model.node.testcase;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Issue implements Node {

	private static final long serialVersionUID = 4701956188777508218L;

	private String content;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("content", getContent() == null ? "" : getContent());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Issue;
	}

}
