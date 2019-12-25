package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.code.Type;

@RelationshipEntity("TYPE_CONTAINS_VARIABLE")
public class TypeContainsVariable implements Relation {
	private static final long serialVersionUID = 3063739285255815579L;
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
	
	private Type type;
	private Variable variable;

	public TypeContainsVariable() {
		super();
	}

	public TypeContainsVariable(Type type, Variable variable) {
		super();
		this.type = type;
		this.variable = variable;
	}

	@Override
	public Long getStartNodeGraphId() {
		return type.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return variable.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.TYPE_CONTAINS_VARIABLE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
