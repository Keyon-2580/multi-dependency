package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FILE_IMPORT_FUNCTION)
public class FileImportFunction implements Relation {

	private static final long serialVersionUID = -4053558454010028825L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	public FileImportFunction(ProjectFile file, Function function) {
		super();
		this.file = file;
		this.function = function;
	}

	@StartNode
	private ProjectFile file;
	
	@EndNode
	private Function function;

	@Override
	public Long getStartNodeGraphId() {
		return file.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return function.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_IMPORT_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
