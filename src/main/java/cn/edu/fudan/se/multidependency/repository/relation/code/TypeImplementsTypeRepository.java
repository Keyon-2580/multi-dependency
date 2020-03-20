package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.code.TypeImplementsType;

@Repository
public interface TypeImplementsTypeRepository extends Neo4jRepository<TypeImplementsType, Long> {

	@Query("MATCH result=(type1:Type)-[r:" + RelationType.str_TYPE_IMPLEMENTS_TYPE + "]->(type2:Type) with type1,type2,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3]->(type1) where id(project)={projectId} RETURN result")
	List<TypeImplementsType> findProjectContainTypeImplementsTypeRelations(@Param("projectId") Long projectId);

}
