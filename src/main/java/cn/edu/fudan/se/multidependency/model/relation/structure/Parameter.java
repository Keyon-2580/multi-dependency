package cn.edu.fudan.se.multidependency.model.relation.structure;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.code.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方法以类型作为方法参数的关系
 * 变量以类型作为类型参数的关系
 * @author fan
 *
 */
@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_PARAMETER)
public class Parameter implements RelationWithTimes {

	private static final long serialVersionUID = -8796616144049338126L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private CodeNode codeNode;
	
	@EndNode
	private Type parameterType;
	
	private int times = 1;

	public Parameter(CodeNode codeNode, Type parameterType) {
		super();
		this.codeNode = codeNode;
		this.parameterType = parameterType;
	}

	@Override
	public Node getStartNode() {
		return codeNode;
	}

	@Override
	public Node getEndNode() {
		return parameterType;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.PARAMETER;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}
	
	public void addTimes() {
		this.times++;
	}

}
