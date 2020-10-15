package cn.edu.fudan.se.multidependency.model.relation.structure;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
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
@RelationshipEntity(RelationType.str_DEPENDENCY)
@EqualsAndHashCode
public class Dependency implements RelationWithTimes, StructureRelation {

	private static final long serialVersionUID = 4817542014996635446L;

	@StartNode
	private CodeNode startCodeNode;

	@EndNode
	private CodeNode endCodeNode;

	private int times = 1;

	private String dependencyTypes;

	public Dependency(CodeNode startCodeNode, CodeNode endCodeNode) {
		super();
		this.startCodeNode = startCodeNode;
		this.endCodeNode = endCodeNode;
		this.times = 1;
		this.dependencyTypes = "";
	}

	public Dependency(CodeNode startCodeNode, CodeNode endCodeNode, String dependencyTypes) {
		super();
		this.startCodeNode = startCodeNode;
		this.endCodeNode = endCodeNode;
		this.times = 1;
		this.dependencyTypes = dependencyTypes;
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
	public void addDependencyType(String dependencyType) {
		this.dependencyTypes +="__"+ dependencyType;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.DEPENDENCY;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		properties.put("dependencyTypes", getDependencyTypes());
		return properties;
	}
}
