package cn.edu.fudan.se.multidependency.model.node.testcase;

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
public class Bug implements Node {

	private static final long serialVersionUID = 4123992909482501778L;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
	@Override
	public Map<String, Object> getProperties() {
		return null;
	}

	@Override
	public NodeType getNodeType() {
		return null;
	}

}
