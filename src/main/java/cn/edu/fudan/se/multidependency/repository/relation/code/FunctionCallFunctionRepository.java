package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;

@Repository
public interface FunctionCallFunctionRepository extends Neo4jRepository<FunctionCallFunction, Long> {
	
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_FUNCTION_CALL_FUNCTION + "]->(function2:Function) with function1,function2,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..5]->(function1) where id(project)={projectId} RETURN result")
	List<FunctionCallFunction> findProjectContainFunctionCallFunctionRelations(@Param("projectId") Long projectId);
	
	/**
	 * 调用了哪些function
	 * @param functionId
	 * @return
	 */
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_FUNCTION_CALL_FUNCTION+ "]->(function2:Function) where id(function1)={functionId} RETURN result")
	List<FunctionCallFunction> queryFunctionCallFunctions(@Param("functionId") long functionId);

	/**
	 * function被哪些调用
	 * @param functionId
	 * @return
	 */
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_FUNCTION_CALL_FUNCTION+ "]->(function2:Function) where id(function2)={functionId} RETURN result")
	List<FunctionCallFunction> queryFunctionCallByFunctions(@Param("functionId") long functionId);
	
	@Query("match (file1:ProjectFile)-[:CONTAIN*2]->(f:Function)-[r:FUNCTION_CALL_FUNCTION]->(function2:Function)<-[:CONTAIN*2]-(file2:ProjectFile) where id(file1)={fileId} and id(file1) <> id(file2) return r,file1,file2")
	Object queryTest(@Param("fileId") long fileId);
	
}
