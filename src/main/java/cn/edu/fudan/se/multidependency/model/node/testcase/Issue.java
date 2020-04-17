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
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("url", getUrl() == null ? "" : getUrl());
		properties.put("repositoryUrl", getRepositoryUrl() == null ? "" : getRepositoryUrl());
		properties.put("labelsUrl", getLabelsUrl() == null ? "" : getLabelsUrl());
		properties.put("commentsUrl", getCommentsUrl() == null ? "" : getCommentsUrl());
		properties.put("eventsUrl", getEventsUrl() == null ? "" : getEventsUrl());
		properties.put("htmlUrl", getHtmlUrl() == null ? "" : getHtmlUrl());
		properties.put("issueId", getIssueId());
		properties.put("issueNodeId", getIssueNodeId() == null ? "" : getIssueNodeId());
		properties.put("number", getNumber());
		properties.put("title", getTitle() == null ? "" : getTitle());
		properties.put("state", getState() == null ? "" : getState());
		properties.put("createTime", getCreateTime() == null ? "" : getCreateTime());
		properties.put("updateTime", getUpdateTime() == null ? "" : getUpdateTime());
		properties.put("closeTime", getCloseTime() == null ? "" : getCloseTime());
		properties.put("body", getBody() == null ? "" : getBody());
		
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
