package cn.edu.fudan.se.multidependency.model.node.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

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
		return new HashMap<>();
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Namespace;
	}

}
