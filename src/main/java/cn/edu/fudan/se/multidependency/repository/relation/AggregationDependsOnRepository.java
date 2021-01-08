package cn.edu.fudan.se.multidependency.repository.relation;

import cn.edu.fudan.se.multidependency.model.relation.AggregationDependsOn;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AggregationDependsOnRepository extends Neo4jRepository<AggregationDependsOn, Long> {
	
	@Query("match p=(:Package)-[r:" + RelationType.str_AGGREGATION_DEPENDS_ON + "]->(:Package) return p")
	List<AggregationDependsOn> findAggregationDependsOn();
}