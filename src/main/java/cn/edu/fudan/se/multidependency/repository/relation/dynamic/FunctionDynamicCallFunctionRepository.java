package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionCallFunction;

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
	
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->(function2:Function) with function1,function2,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..5]->(function1) where id(project)={projectId} RETURN result")
	public List<FunctionDynamicCallFunction> findProjectContainFunctionDynamicCallFunctionRelations(@Param("projectId") Long projectId);

	@Query("match result = (f1:Function)-[r1:" + RelationType.str_FUNCTION_CALL_FUNCTION + "]->(f2:Function) where not (f1)-[:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "{testCaseId:{testcaseId}}]->(f2) return result")
	public List<FunctionCallFunction> findFunctionCallFunctionNotDynamicCalled(@Param("testcaseId") Integer testcaseId);
	
	@Query("match result = (f1:Function)-[r1:" + RelationType.str_FUNCTION_CALL_FUNCTION + "]->(f2:Function) where (f1)-[:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "{testCaseId:{testcaseId}}]->(f2) return result")
	public List<FunctionCallFunction> findFunctionCallFunctionDynamicCalled(@Param("testcaseId") Integer testcaseId);
	
	@Query("match result = (f1:Function)-[r1:" + RelationType.str_FUNCTION_CALL_FUNCTION + "]->(f2:Function) where not (f1)-[:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->(f2) return result")
	public List<FunctionCallFunction> findFunctionCallFunctionNotDynamicCalled();
	
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->(function2:Function) where id(function1)={functionId} return result")
	public List<FunctionDynamicCallFunction> findDynamicCallsByCallerId(@Param("functionId") Long callerFunctionId);
	
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->(function2:Function) where id(function1)={functionId} and r.testCaseId={testCaseId} return result")
	public List<FunctionDynamicCallFunction> findDynamicCallsByCallerIdAndTestCaseId(@Param("functionId") Long callerFunctionId, @Param("testCaseId") Integer testCaseId);
	
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->(function2:Function) where id(function1)={callerId1} and id(function2)={calledId} return result")
	public List<FunctionDynamicCallFunction> findDynamicCallsByCallerIdAndCalledId(@Param("callerId") Long functionCallerId, @Param("calledId") Long functionCalledId);
	
	@Query("MATCH result=(function1:Function)-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]->(function2:Function) where id(function1)={callerId1} and id(function2)={calledId} and r.testCaseId={testCaseId} return result")
	public List<FunctionDynamicCallFunction> findDynamicCallsByCallerIdAndCalledIdAndTestCaseId(@Param("callerId") Long functionCallerId, @Param("calledId") Long functionCalledId, @Param("testCaseId") Integer testCaseId);
	
}
