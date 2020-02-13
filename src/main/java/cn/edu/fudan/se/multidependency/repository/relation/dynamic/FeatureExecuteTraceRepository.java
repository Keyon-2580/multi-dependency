package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Trace;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FeatureExecuteTrace;

@Repository
public interface FeatureExecuteTraceRepository extends Neo4jRepository<FeatureExecuteTrace, Long> {

	@Query("MATCH (f:Feature{featureId:{featureId}})-[r:" + RelationType.str_FEATURE_EXECUTE_TRACE + "]->(t:Trace) RETURN t")
	public Trace findTraceByFeatureId(@Param("featureId") Integer featureId);
	
	@Query("MATCH n=(f:Feature{featureId:{featureId}})-[r:" + RelationType.str_FEATURE_EXECUTE_TRACE + "]->(t:Trace) RETURN n")
	public FeatureExecuteTrace findExecuteTraceByFeatureId(@Param("featureId") Integer featureId);
	
	@Query("MATCH n=(f:Feature)-[:" + RelationType.str_FEATURE_EXECUTE_TRACE + "]->(t:Trace) RETURN n")
	public List<FeatureExecuteTrace> findAllFeatureExecuteTrace();

	@Query("MATCH n=(f:Feature)-[r:" + RelationType.str_FEATURE_EXECUTE_TRACE + "]->(t:Trace) RETURN t")
	public List<Trace> findAllExecuteTraces();
}
