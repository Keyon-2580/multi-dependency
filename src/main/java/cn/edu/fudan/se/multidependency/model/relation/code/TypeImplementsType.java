package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity("TYPE_IMPLEMENTS_TYPE")
public class TypeImplementsType implements Relation {
	
	private static final long serialVersionUID = 3740594031088738257L;

	@Id
    @GeneratedValue
    private Long id;
	
	public TypeImplementsType() {
		super();
	}

	public TypeImplementsType(Type start, Type end) {
		super();
		this.start = start;
		this.end = end;
	}


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

	@Override
	public Long getStartNodeGraphId() {
		return start.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return end.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TYPE_IMPLEMENTS_TYPE;
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