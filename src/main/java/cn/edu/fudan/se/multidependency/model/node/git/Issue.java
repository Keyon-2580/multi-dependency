package cn.edu.fudan.se.multidependency.model.node.git;

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
import org.neo4j.ogm.annotation.Transient;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Issue implements Node {

	private static final long serialVersionUID = 4701956188777508218L;

	@Id
	@GeneratedValue
	private Long id;

	private Long entityId;

	private int number;
	
	private String title;
	
	private String state;

	private String htmlUrl;

	private String createTime;
	
	private String updateTime;
	
	private String closeTime;

	@Transient
	private String body;

	@Transient
	private String developerName;

	public Issue(int number, String title, String state, String htmlUrl,
				 String createTime, String updateTime, String closeTime, String body) {
		this.number = number;
		this.title = title;
		this.state = state;
		this.htmlUrl = htmlUrl;
		this.createTime = createTime;
		this.updateTime = updateTime;
		this.closeTime = closeTime;
		this.body = body;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("number", getNumber());
		properties.put("title", getTitle() == null ? "" : getTitle());
		properties.put("state", getState() == null ? "" : getState());
		properties.put("htmlUrl", getHtmlUrl() == null ? "" : getHtmlUrl());
		properties.put("createTime", getCreateTime() == null ? "" : getCreateTime());
		properties.put("updateTime", getUpdateTime() == null ? "" : getUpdateTime());
		properties.put("closeTime", getCloseTime() == null ? "" : getCloseTime());
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
