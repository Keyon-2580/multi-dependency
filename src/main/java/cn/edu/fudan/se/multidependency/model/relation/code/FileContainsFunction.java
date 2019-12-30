package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.relation.Relation;

@RelationshipEntity(RelationType.str_CONTAIN)
public class FileContainsFunction implements Relation {

	private static final long serialVersionUID = -6154270226333353997L;

	public FileContainsFunction() {
		super();
	}

	public FileContainsFunction(ProjectFile file, Function function) {
		super();
		this.file = file;
		this.function = function;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private ProjectFile file;
	
	@EndNode
	private Function function;

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
		return RelationType.CONTAIN;
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
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}
	
}
