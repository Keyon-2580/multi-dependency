package fan.md.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import fan.md.model.node.code.CodeFile;
import fan.md.model.node.code.Function;
import fan.md.model.relation.Relation;
import fan.md.model.relation.RelationType;

@RelationshipEntity("FILE_CONTAIN_FUNCTION")
public class FileContainFunction implements Relation {

	private static final long serialVersionUID = -6154270226333353997L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private CodeFile file;
	
	@EndNode
	private Function function;

	public CodeFile getFile() {
		return file;
	}

	public void setFile(CodeFile file) {
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
		return RelationType.FILE_CONTAIN_FUNCTION;
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
