package cn.edu.fudan.se.multidependency.model.relation.structure;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.RelationWithTimes;
import cn.edu.fudan.se.multidependency.model.relation.StructureRelation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_USE)
@EqualsAndHashCode
public class Use implements RelationWithTimes, StructureRelation {

	private static final long serialVersionUID = -4490099061857994012L;

	@StartNode
	private CodeNode startCodeNode;

	@EndNode
	private CodeNode endCodeNode;

	private int times = 1;

	private String useType = "";

	public Use(CodeNode startCodeNode, CodeNode endCodeNode) {
		super();
		this.startCodeNode = startCodeNode;
		this.endCodeNode = endCodeNode;
		this.times = 1;
		this.useType = "";
	}

	public Use(CodeNode startCodeNode, CodeNode endCodeNode, String useType) {
		super();
		this.startCodeNode = startCodeNode;
		this.endCodeNode = endCodeNode;
		this.times = 1;
		this.useType = useType;
	}

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public CodeNode getStartCodeNode() {
		return startCodeNode;
	}

	@Override
	public CodeNode getEndCodeNode() {
		return endCodeNode;
	}
	
	public void addTimes() {
		this.times++;
	}

	public void addTimes(int times) {
		this.times += times;
	}

	public void minusTimes() {
		this.times--;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.USE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		properties.put("useType", getUseType());
		return properties;
	}
}
