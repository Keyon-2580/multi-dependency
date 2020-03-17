package cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_SPAN_START_WITH_FUNCTION)
public class SpanStartWithFunction implements Relation {

	private static final long serialVersionUID = 7462518725070039162L;
	
	@Id
    @GeneratedValue
    private Long id;
	
	@StartNode
	private Span span;
	
	@EndNode
	private Function function;
	
	public SpanStartWithFunction(Span span, Function function) {
		this.span = span;
		this.function = function;
	}
	
	@Override
	public Long getStartNodeGraphId() {
		return span.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return function.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.SPAN_START_WITH_FUNCTION;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
