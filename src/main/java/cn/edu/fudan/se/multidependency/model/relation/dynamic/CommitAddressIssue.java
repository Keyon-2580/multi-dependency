package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.testcase.Commit;
import cn.edu.fudan.se.multidependency.model.node.testcase.Issue;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_COMMIT_ADDRESS_ISSUE)
public class CommitAddressIssue implements Relation {

	private static final long serialVersionUID = -3568010370559045060L;

    @Id
    @GeneratedValue
    private Long id;
    
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	@StartNode
	private Commit commit;
	
	@EndNode
	private Issue issue;

	@Override
	public Long getStartNodeGraphId() {
		return commit.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return issue.getId();
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.COMMIT_ADDRESS_ISSUE;
	}

	public Commit getCommit() {
		return commit;
	}

	public void setCommit(Commit commit) {
		this.commit = commit;
	}

	public Issue getIssue() {
		return issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

}
