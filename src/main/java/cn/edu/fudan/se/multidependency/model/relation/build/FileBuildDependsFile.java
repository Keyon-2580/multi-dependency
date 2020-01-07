package cn.edu.fudan.se.multidependency.model.relation.build;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_FILE_BUILD_DEPENDS_FILE)
public class FileBuildDependsFile implements Relation {

	private static final long serialVersionUID = 7074933539766975898L;

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public Long getId() {
		return id;
	}
	
	private ProjectFile start;
	
	private ProjectFile end;
	
	public FileBuildDependsFile() {
		super();
	}
	
	public FileBuildDependsFile(ProjectFile start, ProjectFile end) {
		super();
		this.start = start;
		this.end = end;
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
		return RelationType.FILE_BUILD_DEPENDS_FILE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
