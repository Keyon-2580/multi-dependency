package cn.edu.fudan.se.multidependency.repository.relation.code;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.FunctionParameterType;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionParameterTypeRepository extends Neo4jRepository<FunctionParameterType, Long>{

	@Query("MATCH result=(function:Function)-[r:" + RelationType.str_FUNCTION_PARAMETER_TYPE + "]->(type:Type) with function,type,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..4]->(type) where id(project)={projectId} RETURN result")
	List<FunctionParameterType> findProjectContainFunctionParameterTypeRelations(@Param("projectId") Long projectId);

}
