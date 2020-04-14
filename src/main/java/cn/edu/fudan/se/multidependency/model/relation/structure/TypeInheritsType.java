package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.attoparser.dom.INestableNode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_TYPE_INHERITS_TYPE)
public class TypeInheritsType implements Relation {
	
	private static final long serialVersionUID = 3740594031088738257L;

	@Id
    @GeneratedValue
    private Long id;
	
	private String inheritType;
	
	public static final String INHERIT_TYPE_EXTENDS = "extends";
	public static final String INHERIT_TYPE_IMPLEMENTS = "implements";
	
	public boolean isExtends() {
		return INHERIT_TYPE_EXTENDS.equals(inheritType);
	}
	
	public boolean isImplements() {
		return INHERIT_TYPE_IMPLEMENTS.equals(inheritType);
	}
	
	public TypeInheritsType(Type start, Type end, String inheritType) {
		super();
		this.start = start;
		this.end = end;
		this.inheritType = inheritType;
	}

	@StartNode
	private Type start;
	
	@EndNode
	private Type end;

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
		return RelationType.TYPE_INHERITS_TYPE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("inheritType", getInheritType() == null ? "" : getInheritType());
		return properties;
	}
}
