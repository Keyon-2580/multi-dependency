package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_FILE_IMPORT_VARIABLE)
public class FileImportVariable implements Relation {

	private static final long serialVersionUID = -7712370556388767903L;

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public FileImportVariable(ProjectFile file, Variable variable) {
		super();
		this.file = file;
		this.variable = variable;
	}

	public FileImportVariable() {
		super();
	}

	private ProjectFile file;
	
	private Variable variable;

	@Override
	public Long getStartNodeGraphId() {
		return file.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return variable.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_IMPORT_VARIABLE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	public ProjectFile getFile() {
		return file;
	}

	public void setFile(ProjectFile file) {
		this.file = file;
	}

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}

}
