package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FILE_IMPORT_TYPE)
public class FileImportType implements Relation {

	private static final long serialVersionUID = -7729084310920483342L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private ProjectFile file;

	@EndNode
	private Type type;

	public FileImportType(ProjectFile file, Type type) {
		super();
		this.file = file;
		this.type = type;
	}

	@Override
	public Node getStartNode() {
		return file;
	}

	@Override
	public Node getEndNode() {
		return type;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_IMPORT_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
