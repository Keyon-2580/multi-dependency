package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_DEPENDENCY_FILE_INCLUDE_FILE)
public class FileIncludeFile implements Relation {

	private static final long serialVersionUID = 364395424089272866L;
	@Id
    @GeneratedValue
    private Long id;
	
	private ProjectFile start;
	
	private ProjectFile end;

	public FileIncludeFile() {
		super();
	}
	
	public FileIncludeFile(ProjectFile start, ProjectFile end) {
		super();
		this.start = start;
		this.end = end;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
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
		return RelationType.DEPENDENCY_FILE_INCLUDE_FILE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	public ProjectFile getStart() {
		return start;
	}

	public void setStart(ProjectFile start) {
		this.start = start;
	}

	public ProjectFile getEnd() {
		return end;
	}

	public void setEnd(ProjectFile end) {
		this.end = end;
	}

}
