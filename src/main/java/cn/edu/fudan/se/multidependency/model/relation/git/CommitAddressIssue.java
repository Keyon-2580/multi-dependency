package cn.edu.fudan.se.multidependency.model.relation.git;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_COMMIT_ADDRESS_ISSUE)
public class CommitAddressIssue implements Relation {

	private static final long serialVersionUID = -3568010370559045060L;

    @Id
    @GeneratedValue
    private Long id;
    
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

}
