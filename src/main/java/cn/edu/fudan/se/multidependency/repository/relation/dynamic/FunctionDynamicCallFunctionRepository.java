package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;

@Repository
public interface FunctionDynamicCallFunctionRepository extends Neo4jRepository<FunctionDynamicCallFunction, Long> {

	/**
	 * 找出给定id的方法动态调用了哪些方法
	 * @param id
	 * @return
	 */
	@Query("match (a:Function)-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]-(b:Function) where id(a) = {0} return b")
    List<Function> findCallFunctions(Long id);

	@Query("match p = ()-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->() where r.traceId={traceId} and r.spanId={spanId} return p")
	List<FunctionDynamicCallFunction> findFunctionCallsByTraceIdAndSpanId(@Param("traceId") String traceId, @Param("spanId") String spanId);

	@Query("match p = ()-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->() where r.traceId={traceId} return p")
	List<FunctionDynamicCallFunction> findFunctionCallsByTraceId(@Param("traceId") String traceId);

	@Query("match p = ()-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->() where r.projectName={projectName} and r.language={language} return p")
	List<FunctionDynamicCallFunction> findFunctionCallsByProjectNameAndLanguage(@Param("projectName") String projectName, @Param("language") String language);
	
	@Query("match p = (a:Function)-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->(b:Function)  return a,b,r")
	Map<Function, Map<Function, List<FunctionDynamicCallFunction>>> test();

	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->(function2:Function) with function1,function2,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..4]->(function1) where id(project)={projectId} RETURN result")
	public List<FunctionDynamicCallFunction> findProjectContainFunctionDynamicCallFunctionRelations(@Param("projectId") Long projectId);

}
