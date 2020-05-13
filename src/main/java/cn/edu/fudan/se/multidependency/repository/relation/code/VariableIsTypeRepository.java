package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.VariableIsType;

@Repository
public interface VariableIsTypeRepository extends Neo4jRepository<VariableIsType, Long>{

	@Query("MATCH result=(variable:Variable)-[r:" + RelationType.str_VARIABLE_IS_TYPE + "]->(type:Type) with variable,type,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..4]->(type) where id(project)={projectId} RETURN result")
	List<VariableIsType> findProjectContainVariableIsTypeRelations(@Param("projectId") Long projectId);

}
