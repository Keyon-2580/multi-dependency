package cn.edu.fudan.se.multidependency.model.relation.lib;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FILE_CALL_LIBRARY_API)
public class FileCallLibraryAPI implements Relation {
	private static final long serialVersionUID = 6291017440001369817L;

	@StartNode
	private ProjectFile file;
	
	@EndNode
	private LibraryAPI api;
	
	public FileCallLibraryAPI(ProjectFile file, LibraryAPI api) {
		this.file = file;
		this.api = api;
	}
	
	@Id
    @GeneratedValue
    private Long id;
	
	private int times;

	@Override
	public Long getStartNodeGraphId() {
		return file.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return api.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_CALL_LIBRARY_API;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", times);
		return properties;
	}
}
