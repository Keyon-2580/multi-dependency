package cn.edu.fudan.se.multidependency.model.relation.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;
import cn.edu.fudan.se.multidependency.model.relation.Relation;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RelationshipEntity(RelationType.str_FEATURE_EXECUTE_TRACE)
@EqualsAndHashCode
public class FeatureExecuteTrace implements Relation {

	private static final long serialVersionUID = 8836416426194571527L;

    @Id
    @GeneratedValue
    private Long id;
    
	@StartNode
    private Feature feature;
	
	@EndNode
    private Trace trace;

    public FeatureExecuteTrace(Feature feature, Trace trace) {
    	this.feature = feature;
    	this.trace = trace;
    }
    
	@Override
	public Long getStartNodeGraphId() {
		return feature.getId();
	}

	@Override
	public Long getEndNodeGraphId() {
		return trace.getId();
	}

	@Override
	public RelationType getRelationType() {
		return RelationType.FEATURE_EXECUTE_TRACE;
	}

	@Override
	public Map<String, Object> getProperties() {
		return new HashMap<>();
	}

}
