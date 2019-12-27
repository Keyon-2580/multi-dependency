package cn.edu.fudan.se.multidependency.model.relation.build;

import java.util.Map;

import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity("DEPENDENCY_FILE_BUILD_DEPENDS_FILE")
public class FileBuildDependsFile implements Relation {

	private static final long serialVersionUID = 7074933539766975898L;

	@Override
	public Long getId() {
		return null;
	}

	@Override
	public void setId(Long id) {
		
	}

	@Override
	public Long getStartNodeGraphId() {
		return null;
	}

	@Override
	public Long getEndNodeGraphId() {
		return null;
	}

	@Override
	public RelationType getRelationType() {
		return null;
	}

	@Override
	public Map<String, Object> getProperties() {
		return null;
	}

}
