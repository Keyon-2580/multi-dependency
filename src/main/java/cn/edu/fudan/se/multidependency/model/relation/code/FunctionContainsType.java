package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(RelationType.str_FUNCTION_CONTAINS_TYPE)
public class FunctionContainsType implements Relation {

	private static final long serialVersionUID = 4645473697153791270L;

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
	
	private Function function;
	private Type containType;

	@Override
	public Long getStartNodeGraphId() {
		return function.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return containType.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.CONTAIN;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
