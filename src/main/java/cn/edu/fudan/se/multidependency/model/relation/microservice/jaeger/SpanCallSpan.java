package cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;

@Data
@RelationshipEntity(RelationType.str_SPAN_CALL_SPAN)
public class SpanCallSpan implements Relation {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -24758961478391792L;

	@Id
    @GeneratedValue
    private Long id;
	
	private Span span;
	
	private Span callSpan;
	
	private String time;
	
	@Override
	public Long getStartNodeGraphId() {
		return span.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return callSpan.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.SPAN_CALL_SPAN;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("startServiceName", span.getServiceName());
		properties.put("endServiceName", callSpan.getServiceName());
		properties.put("time", getTime() == null ? "" : getTime());
		return properties;
	}

}
