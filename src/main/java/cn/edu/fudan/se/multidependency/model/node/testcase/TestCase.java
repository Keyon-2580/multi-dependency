package cn.edu.fudan.se.multidependency.model.node.testcase;

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
    
    private Integer testCaseId;
    
    private String description;
    
	public TestCase(Integer testCaseId, String testCaseName, String inputContent, boolean success, String description) {
		this.testCaseId = testCaseId;
		this.testCaseName = testCaseName;
		this.inputContent = inputContent;
		this.success = success;
		this.description = description;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("testCaseName", getTestCaseName() == null ? "" : getTestCaseName());
		properties.put("success", isSuccess());
		properties.put("inputContent", getInputContent() == null ? "" : getInputContent());
		properties.put("testCaseId", getTestCaseId() == null ? -1 : getTestCaseId());
		properties.put("description", getDescription() == null ? "" : getDescription());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.TestCase;
	}
	
	public static final String LABEL_INDEX = "testCaseName";
	@Override
	public String indexName() {
		return LABEL_INDEX;
	}


}
