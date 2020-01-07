package cn.edu.fudan.se.multidependency.model.node.testcase;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeType;

@NodeEntity
public class Commit implements Node {

	private static final long serialVersionUID = 2244271646952758656L;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private String commitId;
    private String message;
    private String person;
//    private Timestamp commitTime;
    ///FIXME
    // 日期、时间
    
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Long getEntityId() {
		return entityId;
	}

	@Override
	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
	    properties.put("commitId", getCommitId() == null ? "" : getCommitId());
	    properties.put("message", getMessage() == null ? "" : getMessage());
	    properties.put("person", getPerson() == null ? "" : getPerson());
//	    properties.put("commitTime", getCommitTime() == null ? "" : getCommitTime());
		return properties;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Commit;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	/*public Timestamp getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(Timestamp commitTime) {
		this.commitTime = commitTime;
	}*/

}
