package fan.md.neo4j.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import fan.md.model.node.code.Function;
import fan.md.model.relation.dynamic.FunctionDynamicCallFunction;

@Repository
public interface FunctionDynamicCallFunctionRepository extends Neo4jRepository<FunctionDynamicCallFunction, Long> {
	
	@Query("match (a:Function)-[r:DYNAMIC_FUNCTION_CALL_FUNCTION]-(b:Function) where id(a) = {0} return b")
    List<Function> findCallFunctions(Long id);

}
