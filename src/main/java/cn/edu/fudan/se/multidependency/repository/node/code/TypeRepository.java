package cn.edu.fudan.se.multidependency.repository.node.code;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.relation.RelationType;

@Repository
public interface TypeRepository extends Neo4jRepository<Type, Long> {
	
	@Query("match (a:ProjectFile)-[r:" + RelationType.str_CONTAIN + "*1..]->(b:Type) where id(b)={typeId} return a")
	public ProjectFile findTypeBelongToFileByTypeId(@Param("typeId") Long typeId);
	
	
}
