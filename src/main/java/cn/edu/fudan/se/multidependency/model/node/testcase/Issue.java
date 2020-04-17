package cn.edu.fudan.se.multidependency.model.node.testcase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
public class Issue implements Node {

	private static final long serialVersionUID = 4701956188777508218L;

	private String url;
	
	private String repositoryUrl;
	
	private String labelsUrl;
	
	private String commentsUrl;
	
	private String eventsUrl;
	
	private String htmlUrl;
	
	private long issueId;
	
	private String issueNodeId;
	
	private int number;
	
	private String title;
	
	private String state;
	
	private String createTime;
	
	private String updateTime;
	
	private String closeTime;
	
	private String body;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("name", getName() == null ? "" : getName());
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("issueId", getIssueId());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Issue;
	}
	
	public static final String LABEL_INDEX = "issueId";
	@Override
	public String indexName() {
		return LABEL_INDEX;
	}

	@Override
	public String getName() {
		return title;
	}


}
