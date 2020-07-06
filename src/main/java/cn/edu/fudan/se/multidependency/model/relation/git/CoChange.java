package cn.edu.fudan.se.multidependency.model.relation.git;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_CO_CHANGE)
public class CoChange implements Relation {
	
	private static final long serialVersionUID = -8677714146194368352L;

	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private ProjectFile file1;
	
	@EndNode
	private ProjectFile file2;
	
	private int times = 1;
	
	public CoChange(ProjectFile file1, ProjectFile file2) {
		this.file1 = file1;
		this.file2 = file2;
	}
	
	public void addTimes() {
		this.times++;
	}

	@Override
	public Node getStartNode() {
		return file1;
	}

	@Override
	public Node getEndNode() {
		return file2;
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.CO_CHANGE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("times", getTimes());
		return properties;
	}
	
	
	
}
