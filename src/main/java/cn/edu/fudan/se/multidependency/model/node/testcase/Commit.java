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
@NoArgsConstructor
@EqualsAndHashCode
public class Commit implements Node {

	private static final long serialVersionUID = 2244271646952758656L;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private String name;
    
    private String commitId;
    
    private String message;
    
    private String person;
    
    private String commitTime;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
	    properties.put("commitId", getCommitId() == null ? "" : getCommitId());
	    properties.put("message", getMessage() == null ? "" : getMessage());
	    properties.put("person", getPerson() == null ? "" : getPerson());
	    properties.put("commitTime", getCommitTime() == null ? "" : getCommitTime());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Commit;
	}
	
	public static final String LABEL_INDEX = "commitTime";
	@Override
	public String indexName() {
		return LABEL_INDEX;
	}


}
