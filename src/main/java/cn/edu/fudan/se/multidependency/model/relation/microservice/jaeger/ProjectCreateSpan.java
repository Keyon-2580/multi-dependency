package cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_PROJECT_CREATE_SPAN)
public class ProjectCreateSpan implements Relation {

	private static final long serialVersionUID = -7559932764276563718L;
	
	public ProjectCreateSpan(Project project, Span span) {
		this.project = project;
		this.span = span;
	}

	@Id
    @GeneratedValue
    private Long id;
		
	private Project project;
	private Span span;

	@Override
	public Long getStartNodeGraphId() {
		return project.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return span.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.PROJECT_CREATE_SPAN;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		return properties;
	}

}
