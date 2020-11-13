package cn.edu.fudan.se.multidependency.model.relation.structure;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
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

	private int times = 0;

	private String dependencyType = "";

	@Properties(allowCast = true)
	private Map<String, Long> dependencyTypesMap = new HashMap<>();

	public Dependency(CodeNode startCodeNode, CodeNode endCodeNode) {
		super();
		this.startCodeNode = startCodeNode;
		this.endCodeNode = endCodeNode;
		this.times = 0;
		this.dependencyType = "";
	}

	public Dependency(CodeNode startCodeNode, CodeNode endCodeNode, String dependencyType) {
		super();
		this.startCodeNode = startCodeNode;
		this.endCodeNode = endCodeNode;
		this.times = 0;
		this.dependencyType = dependencyType;
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
	public void addDependencyType(String dependencyType) {
		if(!this.dependencyType.contains("__" + dependencyType)){
			this.dependencyType += "__" + dependencyType;
		}
	}

	public void addDependencyTypeWithRelation(Relation relation){
		int timesTmp = 0;
		if(relation instanceof RelationWithTimes){
			timesTmp += ((RelationWithTimes) relation).getTimes();
		}else {
			timesTmp += 1;
		}

		Long dependsTimes = dependencyTypesMap.get(relation.getRelationType().toString());
		if (dependsTimes != null){
			dependsTimes += Long.valueOf(timesTmp);
			dependencyTypesMap.put(relation.getRelationType().toString(), dependsTimes);
		} else {
			dependencyTypesMap.put(relation.getRelationType().toString(), Long.valueOf(timesTmp));
		}

		addDependencyType(relation.getRelationType().toString());
		addTimes(timesTmp);
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.DEPENDENCY;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		properties.put("dependencyTypes", getDependencyType());
		properties.putAll(dependencyTypesMap);
		return properties;
	}
}
