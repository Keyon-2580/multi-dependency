package cn.edu.fudan.se.multidependency.model.relation.git;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_COMMIT_UPDATE_FILE)
public class CommitUpdateFile implements Relation {

	private static final long serialVersionUID = -6915005861640573182L;

    @Id
    @GeneratedValue
    private Long id;
    
	@StartNode
	private Commit commit;
	
	@EndNode
	private ProjectFile file;

	private UpdateType updateType;

	public static enum UpdateType {
		ADD, MODIFY, DELETE
	}

	public CommitUpdateFile(Commit commit, ProjectFile file, UpdateType updateType){
		this.commit = commit;
		this.file = file;
		this.updateType = updateType;
	}

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
		Map<String, Object> properties = new HashMap<>();
		properties.put("updateType", getUpdateType().toString() == null ? "" : getUpdateType().toString() );
		return properties;
	}


}
