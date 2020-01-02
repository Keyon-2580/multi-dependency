package cn.edu.fudan.se.multidependency.repository.node.code;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface FunctionRepository extends Neo4jRepository<Function, Long> {
	

	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "*1..]->(b:Function) where id(b)={functionId} return a")
	public ProjectFile findFunctionBelongToFileByFunctionId(@Param("functionId") Long functionId);
}
