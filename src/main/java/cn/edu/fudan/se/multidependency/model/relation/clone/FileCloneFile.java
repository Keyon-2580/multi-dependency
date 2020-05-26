package cn.edu.fudan.se.multidependency.model.relation.clone;

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

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FILE_CLONE_FILE)
public class FileCloneFile implements Relation {
	private static final long serialVersionUID = -8166989684654207651L;

	@Id
    @GeneratedValue
    private Long id;
	 	
	@StartNode
	private ProjectFile file1;
	
	@EndNode
	private ProjectFile file2;
	
	private double value;
	
	public FileCloneFile(ProjectFile file1, ProjectFile file2) {
		this.file1 = file1;
		this.file2 = file2;
	}

	@Override
	public Long getStartNodeGraphId() {
		return file1.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return file2.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_CLONE_FILE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("value", getValue());
		return properties;
	}

}
