package cn.edu.fudan.se.multidependency.repository.relation.dynamic;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.dynamic.FunctionDynamicCallFunction;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionDynamicCallFunctionRepository extends Neo4jRepository<FunctionDynamicCallFunction, Long> {
	
	@Query("match (a:Function)-[r:DYNAMIC_FUNCTION_CALL_FUNCTION]-(b:Function) where id(a) = {0} return b")
    List<Function> findCallFunctions(Long id);

}
