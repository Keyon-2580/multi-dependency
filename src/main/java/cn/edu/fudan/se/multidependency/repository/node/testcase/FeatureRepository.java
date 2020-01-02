package cn.edu.fudan.se.multidependency.repository.node.testcase;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.testcase.Feature;

@Repository
public interface FeatureRepository extends Neo4jRepository<Feature, Long> {
	
	@Query("MATCH (n:Feature{featureName:{0}}) RETURN n")
	public Feature findByFeatureName(String featureName);

}
