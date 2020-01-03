package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionDynamicCallFunctionRepository extends Neo4jRepository<FunctionDynamicCallFunction, Long> {

	/**
	 * 找出给定id的方法动态调用了哪些方法
	 * @param id
	 * @return
	 */
	@Query("match (a:Function)-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "]-(b:Function) where id(a) = {0} return b")
    List<Function> findCallFunctions(Long id);

	/**
	 * 给定测试用例名称，找出该次测试用例中有哪些动态调用的关系
	 * @param testCaseName
	 * @return
	 */
	@Query("MATCH p=()-[r:" + RelationType.str_DYNAMIC_FUNCTION_CALL_FUNCTION + "{testCaseName:{0}}]->() RETURN p")
	List<FunctionDynamicCallFunction> findDynamicCallsByTestCaseName(String testCaseName);
	
}
