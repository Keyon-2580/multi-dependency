package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.testcase.TestCase;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.TestCaseExecuteFeature;

@Repository
public interface TestCaseExecuteFeatureRepository extends Neo4jRepository<TestCaseExecuteFeature, Long> {
	
	@Query("match (a:TestCase)-[r:" + RelationType.str_TESTCASE_EXECUTE_FEATURE + "]-(b:Feature) where id(b) = {featureId} return a")
	List<TestCase> findTestCasesExecuteFeatureByFeatureId(@Param("featureId") Long featureId);
	
	@Query("match (a:TestCase)-[r:" + RelationType.str_TESTCASE_EXECUTE_FEATURE + "]-(b:Feature{featureName:{featureName}}) return a")
	List<TestCase> findTestCasesExecuteFeatureByFeatureName(@Param("featureName") String featureName);
	

}
