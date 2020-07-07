package cn.edu.fudan.se.multidependency.repository.relation.code;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;
import cn.edu.fudan.se.multidependency.model.relation.structure.Inherits;

@Repository
public interface InheritsRepository extends Neo4jRepository<Inherits, Long> {
	
	/**
	 * 找出继承给定id的Type的Type
	 * @param id
	 * @return
	 */
	@Query("match (a:Type)-[r:" + RelationType.str_INHERITS + "]-(b:Type) where id(a) = {0} and r.inheritType=\"" + Inherits.INHERIT_TYPE_EXTENDS + "\" return b")
    List<Type> findExtendsTypesByTypeId(Long id);
	
	/**
	 * 指定type继承哪些type
	 * @param id
	 * @return
	 */
	@Query("match (a:Type)-[r:" + RelationType.str_INHERITS + "]-(b:Type) where id(a) = {0} return b")
    List<Type> findInheritsTypesByTypeId(Long id);
	
	/**
	 * 指定type被哪些继承
	 * @param id
	 * @return
	 */
	@Query("match (a:Type)-[r:" + RelationType.str_INHERITS + "]-(b:Type) where id(b) = {0} return a")
    List<Type> findInheritsFromTypeByTypeId(Long id);
	
	@Query("MATCH result=(type1:Type)-[r:" + RelationType.str_INHERITS + "]->(type2:Type) with type1,type2,result match (project:Project)-[r2:" + RelationType.str_CONTAIN + "*3..4]->(type1) where id(project)={projectId} RETURN result")
	List<Inherits> findProjectContainTypeInheritsTypeRelations(@Param("projectId") Long projectId);

	@Query("match p=(a:Type)-[r:" + RelationType.str_INHERITS + "*1..]->(b:Type) where id(a)={subTypeId} and id(b)={superTypeId} return b;")
	Type findIsTypeInheritsType(@Param("subTypeId") Long subTypeId, @Param("superTypeId") Long superTypeId);
	
	@Query("match (file1:ProjectFile)-[:CONTAIN]->(t:Type)-[r:TYPE_INHERITS_TYPE]->(types:Type)<-[:CONTAIN]-(file2:ProjectFile) where  id(file1) <> id(file2) return r,file1,file2")
	List<Object> test();
}