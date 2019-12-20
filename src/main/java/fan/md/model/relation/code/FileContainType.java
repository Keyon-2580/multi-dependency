package fan.md.model.relation.code;

import java.io.Serializable;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import fan.md.model.entity.code.CodeFile;
import fan.md.model.entity.code.Type;

@RelationshipEntity("FILE_CONTAIN_TYPE")
public class FileContainType implements Serializable {

	private static final long serialVersionUID = 1653809506761293660L;

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
	
	
}
