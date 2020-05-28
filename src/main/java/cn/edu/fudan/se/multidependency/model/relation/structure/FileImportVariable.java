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
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FILE_IMPORT_VARIABLE)
public class FileImportVariable implements Relation {

	private static final long serialVersionUID = -7712370556388767903L;

	@Id
    @GeneratedValue
    private Long id;
	
	public FileImportVariable(ProjectFile file, Variable variable) {
		super();
		this.file = file;
		this.variable = variable;
	}

	@StartNode
	private ProjectFile file;
	
	@EndNode
	private Variable variable;

	@Override
	public Node getStartNode() {
		return file;
	}

	@Override
	public Node getEndNode() {
		return variable;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_IMPORT_VARIABLE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
