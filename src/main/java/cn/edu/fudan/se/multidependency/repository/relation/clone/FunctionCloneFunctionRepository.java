package cn.edu.fudan.se.multidependency.repository.relation.clone;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.clone.FunctionCloneFunction;

@Repository
public interface FunctionCloneFunctionRepository extends Neo4jRepository<FunctionCloneFunction, Long> {
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_FUNCTION_CLONE_FUNCTION + "]->(function2:Function) with function1,function2,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..4]->(function1) where id(project)={projectId} with function1, function2, result match (project:Project)-[r3:" + RelationType.str_CONTAIN + "*3..4]->(function2) where id(project)={projectId} RETURN result")
	public List<FunctionCloneFunction> findProjectContainFunctionCloneFunctionRelations(@Param("projectId") Long projectId);

}