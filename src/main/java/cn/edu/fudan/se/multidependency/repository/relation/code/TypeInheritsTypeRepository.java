package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.TypeInheritsType;

@Repository
public interface TypeInheritsTypeRepository extends Neo4jRepository<TypeInheritsType, Long> {
	
	/**
	 * 找出继承给定id的Type的Type
	 * @param id
	 * @return
	 */
	@Query("match (a:Type)-[r:" + RelationType.str_TYPE_INHERITS_TYPE + "]-(b:Type) where id(a) = {0} and r.inheritType=\"" + TypeInheritsType.INHERIT_TYPE_EXTENDS + "\" return b")
    List<Type> findExtendsTypesByTypeId(Long id);
	
	@Query("MATCH result=(type1:Type)-[r:" + RelationType.str_TYPE_INHERITS_TYPE + "]->(type2:Type) with type1,type2,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..4]->(type1) where id(project)={projectId} RETURN result")
	List<TypeInheritsType> findProjectContainTypeInheritsTypeRelations(@Param("projectId") Long projectId);

	@Query("match p=(a:Type)-[r:" + RelationType.str_TYPE_INHERITS_TYPE + "*1..]->(b:Type) where id(a)={subTypeId} and id(b)={superTypeId} return b;")
	Type findIsTypeInheritsType(@Param("subTypeId") Long subTypeId, @Param("superTypeId") Long superTypeId);
}
