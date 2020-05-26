package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionAccessField;

public interface FunctionAccessFieldRepository extends Neo4jRepository<FunctionAccessField, Long> {
	
	@Query("MATCH result=(function:Function)-[r:" + RelationType.str_FUNCTION_ACCESS_FIELD + "]->(field:Variable) with function,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..5]->(function) where id(project)={projectId} RETURN result")
	List<FunctionAccessField> findProjectContainFunctionAccessFieldRelations(@Param("projectId") Long projectId);

}
