package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipEntity(RelationType.str_FILE_INCLUDE_FILE)
@Data
@NoArgsConstructor
public class FileIncludeFile implements Relation {

	private static final long serialVersionUID = 364395424089272866L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private ProjectFile start;
	
	@EndNode
	private ProjectFile end;

	public FileIncludeFile(ProjectFile start, ProjectFile end) {
		super();
		this.start = start;
		this.end = end;
	}
	
	@Override
	public Long getStartNodeGraphId() {
		return start.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return end.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_INCLUDE_FILE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
