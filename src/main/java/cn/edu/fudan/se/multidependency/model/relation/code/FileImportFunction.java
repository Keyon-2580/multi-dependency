package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_DEPENDENCY_FILE_IMPORT_FUNCTION)
public class FileImportFunction implements Relation {

	private static final long serialVersionUID = -4053558454010028825L;
	
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
	
	public FileImportFunction(ProjectFile file, Function function) {
		super();
		this.file = file;
		this.function = function;
	}

	public FileImportFunction() {
		super();
	}

	private ProjectFile file;
	
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
		return RelationType.DEPENDENCY_FILE_IMPORT_FUNCTION;
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

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

}
