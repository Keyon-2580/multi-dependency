package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Extends;

@Repository
public interface ExtendsRepository extends Neo4jRepository<Extends, Long> {
	
	@Query("match (a:Type)-[r:" + RelationType.str_EXTENDS + "]-(b:Type) where id(b) = {id} return a")
    List<Type> findWhatExtendsType(@Param("id") Long id);
	
	@Query("match (a:Type)-[r:" + RelationType.str_EXTENDS + "]-(b:Type) where id(a) = {id} return b")
    List<Type> findTypeExtendsWhat(@Param("id") Long id);
	
	@Query("MATCH result=(type1:Type)-[r:" + RelationType.str_EXTENDS + "]->(type2:Type) with type1,type2,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..4]->(type1) where id(project)={projectId} RETURN result")
	List<Extends> findProjectContainTypeExtendsTypeRelations(@Param("projectId") Long projectId);

	@Query("match p=(a:Type)-[r:" + RelationType.str_EXTENDS + "*1..]->(b:Type) where id(a)={subTypeId} and id(b)={superTypeId} return b;")
	Type findIsTypeExtendsType(@Param("subTypeId") Long subTypeId, @Param("superTypeId") Long superTypeId);
	
}
