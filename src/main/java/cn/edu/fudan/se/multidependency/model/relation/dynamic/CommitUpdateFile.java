package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.testcase.Commit;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_COMMIT_UPDATE_FILE)
public class CommitUpdateFile implements Relation {

	private static final long serialVersionUID = -6915005861640573182L;

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
	
	private Commit commit;
	private ProjectFile file;

	@Override
	public Long getStartNodeGraphId() {
		return commit.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return file.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.COMMIT_UPDATE_FILE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	public Commit getCommit() {
		return commit;
	}

	public void setCommit(Commit commit) {
		this.commit = commit;
	}

	public ProjectFile getFile() {
		return file;
	}

	public void setFile(ProjectFile file) {
		this.file = file;
	}

}
