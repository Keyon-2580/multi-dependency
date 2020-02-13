package cn.edu.fudan.se.multidependency.repository.relation.microservice.jaeger;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.microservice.jaeger.MicroServiceCreateSpan;

@Repository
public interface ProjectCreateSpanRepository extends Neo4jRepository<MicroServiceCreateSpan, Long>{
	
	@Query("MATCH n = (m:MicroService)-[r:" + RelationType.str_MICRO_SERVICE_CREATE_SPAN + "]->(s:Span{spanId:{spanId}}) return n")
	public MicroServiceCreateSpan findProjectCreateSpan(@Param("spanId") String spanId);

}
