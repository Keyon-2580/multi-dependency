package cn.edu.fudan.se.multidependency.repository.node.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Function;

@Repository
public interface FunctionRepository extends Neo4jRepository<Function, Long> {

	@Query("match (a:Function) return a")
	public List<Function> findAllFunctionsList();
}
