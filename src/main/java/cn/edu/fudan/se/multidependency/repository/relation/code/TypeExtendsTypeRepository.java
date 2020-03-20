package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.TypeExtendsType;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Type;

@Repository
public interface TypeExtendsTypeRepository extends Neo4jRepository<TypeExtendsType, Long> {
	
	/**
	 * 找出继承给定id的Type的Type
	 * @param id
	 * @return
	 */
	@Query("match (a:Type)-[r:TYPE_EXTENDS_TYPE]-(b:Type) where id(a) = {0} return b")
    List<Type> findExtendsTypesByTypeId(Long id);
	
	@Query("MATCH result=(type1:Type)-[r:" + RelationType.str_TYPE_EXTENDS_TYPE + "]->(type2:Type) with type1,type2,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3]->(type1) where id(project)={projectId} RETURN result")
	List<TypeExtendsType> findProjectContainTypeExtendsTypeRelations(@Param("projectId") Long projectId);
	
}
