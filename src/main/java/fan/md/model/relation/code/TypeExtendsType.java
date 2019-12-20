package fan.md.model.relation.code;

import java.io.Serializable;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import fan.md.model.entity.code.Type;

@RelationshipEntity("TYPE_EXTENDS_TYPE")
public class TypeExtendsType implements Serializable {
	
	private static final long serialVersionUID = 3740594031088738257L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Type start;
	
	@EndNode
	private Type end;

	public Type getStart() {
		return start;
	}

	public void setStart(Type start) {
		this.start = start;
	}

	public Type getEnd() {
		return end;
	}

	public void setEnd(Type end) {
		this.end = end;
	}
	
}
