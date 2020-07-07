package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 该implement为C/C++中的.c/cpp文件内的方法 implement .h文件内声明的方法
 * 不是类实现接口，实现接口的关系在inherits类中
 * @author fan
 *
 */
@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_IMPLEMENT)
@EqualsAndHashCode
public class Implement implements StructureRelation {
	
	private static final long serialVersionUID = 7582417525375943056L;

	@Id
    @GeneratedValue
    private Long id;
	
	public Implement(Function function, Function implementFunction) {
		this.function = function;
		this.implementFunction = implementFunction;
	}
	
	@StartNode
	private Function function;
	
	@EndNode
	private Function implementFunction;

	@Override
	public CodeNode getStartCodeNode() {
		return function;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return implementFunction;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.IMPLEMENT;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}
	
}
