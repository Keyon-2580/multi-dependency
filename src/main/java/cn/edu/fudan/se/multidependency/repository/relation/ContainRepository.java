package cn.edu.fudan.se.multidependency.repository.relation;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.microservice.jaeger.Span;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface ContainRepository extends Neo4jRepository<Contain, Long> {

	@Query("MATCH (t:Trace{traceId:{traceId}})-[r:" + RelationType.str_CONTAIN + "]->(s:Span) RETURN s")
	public List<Span> findSpansByTraceId(@Param("traceId") String traceId);
	
}
