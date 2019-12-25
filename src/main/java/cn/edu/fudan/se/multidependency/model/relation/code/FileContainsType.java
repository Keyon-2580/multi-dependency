package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.CodeFile;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.code.Type;

@RelationshipEntity("FILE_CONTAINS_TYPE")
public class FileContainsType implements Relation {

	private static final long serialVersionUID = 1653809506761293660L;

	public FileContainsType() {
		super();
	}

	public FileContainsType(CodeFile file, Type type) {
		super();
		this.file = file;
		this.type = type;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private CodeFile file;
	
	@EndNode
	private Type type;

	public CodeFile getFile() {
		return file;
	}

	public void setFile(CodeFile file) {
		this.file = file;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public Long getStartNodeGraphId() {
		return file.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return type.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FILE_CONTAINS_TYPE;
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
