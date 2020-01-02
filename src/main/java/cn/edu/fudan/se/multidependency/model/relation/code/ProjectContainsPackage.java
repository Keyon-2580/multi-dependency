package cn.edu.fudan.se.multidependency.model.relation.code;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@RelationshipEntity(RelationType.str_PROJECT_CONTAINS_PACKAGE)
public class ProjectContainsPackage implements Relation {

	private static final long serialVersionUID = 5380708098150213268L;

	private Project project;
	
	private Package pck;
	
	public ProjectContainsPackage() {
		super();
	}

	public ProjectContainsPackage(Project project, Package pck) {
		super();
		this.project = project;
		this.pck = pck;
	}

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

	@Override
	public Long getStartNodeGraphId() {
		return project.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return pck.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.PROJECT_CONTAINS_PACKAGE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Package getPck() {
		return pck;
	}

	public void setPck(Package pck) {
		this.pck = pck;
	}

}
