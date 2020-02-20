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
@EqualsAndHashCode
@NoArgsConstructor
public class TestCase implements Node {

	private static final long serialVersionUID = 7817933207475762644L;
	
	private String testCaseName;
	
	private String inputContent;
	
	private boolean success;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private Long testCaseId;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("testCaseName", getTestCaseName() == null ? "" : getTestCaseName());
		properties.put("success", isSuccess());
		properties.put("inputContent", getInputContent() == null ? "" : getInputContent());
		properties.put("testCaseId", getTestCaseId() == null ? -1 : getTestCaseId());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.TestCase;
	}

}
