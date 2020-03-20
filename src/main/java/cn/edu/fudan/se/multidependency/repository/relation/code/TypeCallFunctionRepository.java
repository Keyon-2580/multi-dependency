package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeCallFunction;

@Repository
public interface TypeCallFunctionRepository extends Neo4jRepository<TypeCallFunction, Long> {
	@Query("MATCH result=(type:Type)-[r:" + RelationType.str_TYPE_CALL_FUNCTION + "]->(function:Function) with type,function,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3]->(type) where id(project)={projectId} RETURN result")
	public List<TypeCallFunction> findProjectContainTypeCallFunctionRelations(@Param("projectId") Long projectId);
}