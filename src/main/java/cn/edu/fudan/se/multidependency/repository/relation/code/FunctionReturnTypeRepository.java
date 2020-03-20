package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionReturnType;

@Repository
public interface FunctionReturnTypeRepository extends Neo4jRepository<FunctionReturnType, Long> {

	@Query("MATCH result=(function:Function)-[r:" + RelationType.str_FUNCTION_RETURN_TYPE + "]->(type:Type) with function,type,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3]->(type) where id(project)={projectId} RETURN result")
	List<FunctionReturnType> findProjectContainFunctionReturnTypeRelations(@Param("projectId") Long projectId);

}
